
import { useEffect, useState } from "react";
import { masterApi, processApi, roomApi } from "../../api";
import type { Master, MasterForm } from "../../types/Master.ts";
import type { Processus } from "../../types/Processus.ts";
import type { Room } from "../../types/Room.ts";
import useNotification from "../../hooks/useNotification.ts";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, { type Column } from "../../components/generic/display/Table.tsx";
import MasterPopup from "../../components/master/MasterPopup.tsx";
import Confirmation from "../../components/generic/overlay/Confirmation.tsx";

// Liste des masters avec leurs processus associés et leur plaquage dans une salle
export default function Masters() {
    const { notify } = useNotification();

    const [processes, setProcesses] = useState<Processus[]>([]);
    const [creating, setCreating] = useState(false);
    const [saving, setSaving] = useState(false);

    const [selectedMaster, setSelectedMaster] = useState<Master | null>(null);
    const [rooms, setRooms] = useState<Room[]>([]);
    const [selectedRoomId, setSelectedRoomId] = useState("");
    const [showRoomSelection, setShowRoomSelection] = useState(false);
    const [showConfirmation, setShowConfirmation] = useState(false);
    const [loadingRooms, setLoadingRooms] = useState(false);
    const [reinstalling, setReinstalling] = useState(false);

    const selectedRoom = rooms.find(
        (room) => String(room.id) === selectedRoomId
    );

    const {
        search,
        items: masters,
        loading,
        page,
        totalPages,
        totalElements,
        setPage,
        setSearch,
        reload,
    } = usePaginatedList({
        load: masterApi.list,
        initialFilters: {},
        pageSize: 10,
        errorMessage: "Impossible de charger les masters.",
        initialSort: "nom,asc",
    });

    // Charge tous les processus pour afficher leur nom à partir des identifiants du master
    useEffect(() => {
        let cancelled = false;

        processApi.list({
            page: 0,
            size: 1000,
            sort: "nom,asc",
        })
            .then((result) => {
                if (!cancelled) {
                    setProcesses(result.content);
                }
            })
            .catch(() => {
                if (!cancelled) {
                    notify("Impossible de charger les processus.", "danger");
                }
            });

        return () => {
            cancelled = true;
        };
    }, [notify]);

    async function saveMaster(form: MasterForm) {
        setSaving(true);

        try {
            await masterApi.create(form);

            notify("Master créé.", "success");
            setCreating(false);
            reload();
        } catch {
            notify("Impossible de créer le master.", "danger");
        } finally {
            setSaving(false);
        }
    }

    // Ouvre la sélection de salle pour le master choisi
    async function openRoomSelection(master: Master) {
        setSelectedMaster(master);
        setSelectedRoomId("");
        setRooms([]);
        setShowRoomSelection(true);
        setLoadingRooms(true);

        try {
            const result = await roomApi.list({
                page: 0,
                size: 1000,
                sort: "nom,asc",
            });

            setRooms(
                result.content.filter((room) => room.typeSalle === "COURS")
            );
        } catch {
            notify("Impossible de charger les salles.", "danger");
            setShowRoomSelection(false);
        } finally {
            setLoadingRooms(false);
        }
    }

    // Lance la réinstallation de la salle sélectionnée
    async function reinstallRoom() {
        if (!selectedRoom) return;

        setReinstalling(true);

        try {
            await roomApi.reinstall(selectedRoom.id);

            notify(
                `Réinstallation de la salle ${selectedRoom.nom} lancée.`,
                "success"
            );

            setShowConfirmation(false);
            setShowRoomSelection(false);
            setSelectedMaster(null);
            setSelectedRoomId("");
        } catch {
            notify(
                "Impossible de lancer la réinstallation de la salle.",
                "danger"
            );
        } finally {
            setReinstalling(false);
        }
    }

    const columns = [
        {
            key: "nom",
            label: "Master",
        },
        {
            label: "Processus associés",
            render: (master: Master) =>
                master.processusId.length
                    ? master.processusId
                        .map((id) =>
                            processes.find((process) => process.id === id)?.nom
                            ?? `#${id}`
                        )
                        .join(", ")
                    : (
                        <span className="text-body-secondary">
                            Aucun processus
                        </span>
                    ),
        },
        {
            label: "Action",
            render: (master: Master) => (
                <button
                    type="button"
                    className="btn btn-sm btn-outline-primary text-nowrap"
                    onClick={() => void openRoomSelection(master)}
                    disabled={loadingRooms}
                >
                    <i className="bi bi-pc-display me-2" />
                    Plaquer
                </button>
            ),
        },
    ] satisfies Column<Master>[];

    return (
        <>
            <PageHeader
                title="Masters"
                description="Gérer les masters et leurs processus associés."
                actions={[
                    {
                        label: "Ajouter un master",
                        icon: "bi-plus-lg",
                        variant: "primary",
                        onClick: () => setCreating(true),
                    },
                ]}
            />

            <Filters
                search={search}
                searchPlaceholder="Rechercher un master..."
                values={{}}
                onSearchChange={setSearch}
                onFilterChange={() => {}}
                onReset={() => setSearch("")}
            />

            {showRoomSelection && selectedMaster && (
                <section className="card border mb-3">
                    <div className="card-body">
                        <div className="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h2 className="h5 mb-1">
                                    Plaquer un master
                                </h2>
                                <p className="text-body-secondary mb-0">
                                    Master sélectionné :{" "}
                                    <strong>{selectedMaster.nom}</strong>
                                </p>
                            </div>

                            <button
                                type="button"
                                className="btn-close"
                                aria-label="Fermer"
                                onClick={() => {
                                    setShowRoomSelection(false);
                                    setSelectedMaster(null);
                                }}
                            />
                        </div>

                        <div className="row g-3 align-items-end">
                            <div className="col-12 col-md-8">
                                <label
                                    htmlFor="master-room"
                                    className="form-label"
                                >
                                    Salle de destination
                                </label>

                                <select
                                    id="master-room"
                                    className="form-select"
                                    value={selectedRoomId}
                                    onChange={(event) =>
                                        setSelectedRoomId(event.target.value)
                                    }
                                    disabled={loadingRooms}
                                >
                                    <option value="">
                                        Sélectionner une salle
                                    </option>

                                    {rooms.map((room) => (
                                        <option
                                            key={room.id}
                                            value={room.id}
                                        >
                                            {room.nom} - {room.nomBatiment}
                                        </option>
                                    ))}
                                </select>

                                {!loadingRooms && rooms.length === 0 && (
                                    <div className="form-text">
                                        Aucune salle de cours disponible.
                                    </div>
                                )}

                                {loadingRooms && (
                                    <div className="form-text">
                                        Chargement des salles...
                                    </div>
                                )}
                            </div>

                            <div className="col-12 col-md-4 d-flex gap-2">
                                <button
                                    type="button"
                                    className="btn btn-outline-secondary"
                                    onClick={() => {
                                        setShowRoomSelection(false);
                                        setSelectedMaster(null);
                                    }}
                                >
                                    Annuler
                                </button>

                                <button
                                    type="button"
                                    className="btn btn-primary"
                                    disabled={
                                        !selectedRoomId ||
                                        loadingRooms ||
                                        reinstalling
                                    }
                                    onClick={() => setShowConfirmation(true)}
                                >
                                    <i className="bi bi-arrow-repeat me-2" />
                                    Plaquer
                                </button>
                            </div>
                        </div>
                    </div>
                </section>
            )}

            <Table<Master>
                columns={columns}
                data={masters}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />

            <Confirmation
                open={showConfirmation}
                title="Réinstaller la salle ?"
                message={`Tous les postes de la salle ${selectedRoom?.nom ?? ""} seront réinstallés avec le master ${selectedMaster?.nom ?? ""}.`}
                onCancel={() => setShowConfirmation(false)}
                onConfirm={() => void reinstallRoom()}
            />

            {creating && (
                <MasterPopup
                    processes={processes}
                    saving={saving}
                    onClose={() => setCreating(false)}
                    onSubmit={(form) => void saveMaster(form)}
                />
            )}
        </>
    );
}
