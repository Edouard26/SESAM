import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request

import psutil

MAX_NOM_PROCESSUS = 30
UTILISATEURS_SYSTEME = ("NT AUTHORITY", "NT SERVICE", "Window Manager", "Font Driver Host")
PROCESSUS_PROTEGES = {
    "system", "system idle process", "registry", "smss.exe", "csrss.exe", "wininit.exe",
    "winlogon.exe", "services.exe", "lsass.exe", "svchost.exe", "dwm.exe", "explorer.exe",
    "fontdrvhost.exe", "memcompression", "secure system",
}


class ApiClient:
    def __init__(self, base_url, email, password):
        self.base_url = base_url.rstrip("/")
        self.email = email
        self.password = password
        self.token = None

    def login(self):
        reponse = self._requete("POST", "/auth/login/", {"email": self.email, "password": self.password}, auth=False)
        self.token = reponse["accessToken"]

    def get(self, chemin):
        return self._avec_auth("GET", chemin)

    def put(self, chemin, corps):
        return self._avec_auth("PUT", chemin, corps)

    def patch(self, chemin, corps):
        return self._avec_auth("PATCH", chemin, corps)

    def _avec_auth(self, methode, chemin, corps=None):
        if self.token is None:
            self.login()
        try:
            return self._requete(methode, chemin, corps)
        except urllib.error.HTTPError as e:
            if e.code not in (401, 403):
                raise
            self.login()
            return self._requete(methode, chemin, corps)

    def _requete(self, methode, chemin, corps=None, auth=True):
        donnees = json.dumps(corps).encode("utf-8") if corps is not None else None
        requete = urllib.request.Request(self.base_url + chemin, data=donnees, method=methode)
        requete.add_header("Content-Type", "application/json")
        if auth:
            requete.add_header("Authorization", "Bearer " + self.token)
        with urllib.request.urlopen(requete, timeout=10) as reponse:
            contenu = reponse.read()
        return json.loads(contenu) if contenu else None


def est_interne_windows(processus):
    infos = processus.info
    exe = infos.get("exe")
    if not exe:
        return True
    racine = os.environ.get("SystemRoot", r"C:\Windows").lower()
    if os.path.normcase(exe).startswith(os.path.normcase(racine) + os.sep):
        return True
    utilisateur = infos.get("username") or ""
    return utilisateur.startswith(UTILISATEURS_SYSTEME)


def lister_processus_utilisateur():
    noms = set()
    for processus in psutil.process_iter(["name", "exe", "username"]):
        nom = processus.info.get("name")
        if not nom or len(nom) > MAX_NOM_PROCESSUS or est_interne_windows(processus):
            continue
        noms.add(nom)
    return sorted(noms, key=str.lower)


def pids_a_epargner():
    pids = {os.getpid()}
    try:
        pids.update(p.pid for p in psutil.Process().parents())
    except psutil.Error:
        pass
    return pids


def fermer_processus_interdits(noms_interdits, dry_run):
    cibles = {nom.lower() for nom in noms_interdits} - PROCESSUS_PROTEGES
    epargnes = pids_a_epargner()
    fermes = []
    for processus in psutil.process_iter(["name"]):
        nom = processus.info.get("name")
        if not nom or nom.lower() not in cibles or processus.pid in epargnes:
            continue
        if dry_run:
            fermes.append((nom, processus.pid))
            continue
        try:
            processus.terminate()
            processus.wait(timeout=3)
        except psutil.TimeoutExpired:
            processus.kill()
        except psutil.Error as e:
            print(f"  impossible de fermer {nom} (pid {processus.pid}) : {e}")
            continue
        fermes.append((nom, processus.pid))
    return fermes


def appliquer_regles(interdits, dry_run):
    for nom, pid in fermer_processus_interdits(interdits, dry_run):
        print(f"  {'[dry-run] ' if dry_run else ''}ferme : {nom} (pid {pid})")


def main():
    parser = argparse.ArgumentParser(description="Agent SESAM : remonte les processus d'une UC et applique les regles actives")
    parser.add_argument("--api", default=os.environ.get("SESAM_API", "http://localhost:8080/api/v1"))
    parser.add_argument("--uc-id", type=int, default=int(os.environ.get("SESAM_UC_ID", "43")))
    parser.add_argument("--email", default=os.environ.get("SESAM_EMAIL", "gestionnaire@intradef.gouv.fr"))
    parser.add_argument("--password", default=os.environ.get("SESAM_PASSWORD", "gestionnaire"))
    parser.add_argument("--interval", type=int, default=5, help="secondes entre deux scans (0 = un seul scan)")
    parser.add_argument("--dry-run", action="store_true", help="n'arrete aucun processus")
    args = parser.parse_args()

    client = ApiClient(args.api, args.email, args.password)
    connus = None
    interdits = []
    while True:
        try:
            actuels = set(lister_processus_utilisateur())
            if connus is None:
                client.put(f"/UCs/{args.uc_id}/processes/", {"processus": sorted(actuels)})
                interdits = client.get(f"/UCs/{args.uc_id}/forbidden-processes/")
                print(f"{len(actuels)} processus remontes pour l'UC {args.uc_id}")
            elif actuels != connus:
                started, stopped = sorted(actuels - connus), sorted(connus - actuels)
                interdits = client.patch(f"/UCs/{args.uc_id}/processes/", {"started": started, "stopped": stopped})
                print(f"+{started} -{stopped}")
            else:
                interdits = client.get(f"/UCs/{args.uc_id}/forbidden-processes/")
            connus = actuels
        except (urllib.error.URLError, OSError) as e:
            print(f"Erreur API : {e}", file=sys.stderr)
            connus = None
        appliquer_regles(interdits, args.dry_run)
        if args.interval <= 0:
            break
        time.sleep(args.interval)


if __name__ == "__main__":
    main()
