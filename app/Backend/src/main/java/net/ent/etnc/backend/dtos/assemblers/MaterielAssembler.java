package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.MaterielRequest;
import net.ent.etnc.backend.dtos.responses.MaterielResponse;
import net.ent.etnc.backend.models.entities.Ecran;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.models.entities.VideoProjecteur;
import org.springframework.stereotype.Component;

@Component
public class MaterielAssembler {

    public MaterielResponse toResponse(Materiel materiel){
        switch (materiel.getClass().getSimpleName()){
            case "UniteCentrale" -> {
                return toResponseUniteCentrale((UniteCentrale) materiel);
            }
            case "Ecran" -> {
                return toResponseEcran((Ecran) materiel);
            }
            case "VideoProjecteur" -> {
                return toResponseVideoProjecteur((VideoProjecteur) materiel);
            }
            default -> {
                return null;
            }
        }
    }

    public MaterielResponse toResponseUniteCentrale(UniteCentrale uniteCentrale){
        return responseBuilder(uniteCentrale)
                .processeur(uniteCentrale.getProcesseur())
                .carteGraphique(uniteCentrale.getCarteGraphique())
                .build();
    }

    public MaterielResponse toResponseEcran(Ecran ecran){
        return responseBuilder(ecran)
                .taille(ecran.getTaille())
                .build();
    }

    public MaterielResponse toResponseVideoProjecteur(VideoProjecteur videoProjecteur){
        return responseBuilder(videoProjecteur)
                .build();
    }

    private MaterielResponse.MaterielResponseBuilder responseBuilder(Materiel materiel) {
        return MaterielResponse.builder()
                .id(materiel.getId())
                .typeMateriel(materiel.getClass().getSimpleName())
                .numeroSerie(materiel.getNumeroSerie())
                .nom(materiel.getNom())
                .dateAjout(materiel.getDataAjout())
                .dateFinGarantie(materiel.getDateFinGarantie())
                .marque(materiel.getMarque())
                .modele(materiel.getModele())
                .etat(materiel.getEtat());
    }

//    public List<MaterielResponse> toResponses(Collection<Materiel> materiels) {
//        return materiels.stream().map(this::toResponse).toList();
//    }

    public MaterielRequest toRequest(Materiel materiel){
        return MaterielRequest.builder()
                .typeMateriel(materiel.getClass().getSimpleName())
                .numeroSerie(materiel.getNumeroSerie())
                .nom(materiel.getNom())
                .dateAjout(materiel.getDataAjout())
                .dateFinGarantie(materiel.getDateFinGarantie())
                .marque(materiel.getMarque())
                .modele(materiel.getModele())
                .etat(materiel.getEtat())
                .build();
    }

    public UniteCentrale toUniteCentrale(MaterielRequest materielRequest) {
        UniteCentrale uc = new UniteCentrale();

        uc.setNumeroSerie(materielRequest.getNumeroSerie());
        uc.setNom(materielRequest.getNom());
        uc.setModele(materielRequest.getModele());
        uc.setMarque(materielRequest.getMarque());
        uc.setDataAjout(materielRequest.getDateAjout());
        uc.setDateFinGarantie(materielRequest.getDateFinGarantie());
        uc.setEtat(materielRequest.getEtat());
        uc.setProcesseur(materielRequest.getProcesseur());
        uc.setCarteGraphique(materielRequest.getCarteGraphique());

        return uc;
    }

    public Ecran toEcran(MaterielRequest materielRequest) {
        Ecran ecran = new Ecran();

        ecran.setNumeroSerie(materielRequest.getNumeroSerie());
        ecran.setNom(materielRequest.getNom());
        ecran.setModele(materielRequest.getModele());
        ecran.setMarque(materielRequest.getMarque());
        ecran.setDataAjout(materielRequest.getDateAjout());
        ecran.setDateFinGarantie(materielRequest.getDateFinGarantie());
        ecran.setEtat(materielRequest.getEtat());
        ecran.setTaille(materielRequest.getTaille());

        return ecran;
    }

    public VideoProjecteur toVideoProjecteur(MaterielRequest materielRequest) {
        VideoProjecteur videoProjecteur = new VideoProjecteur();

        videoProjecteur.setNumeroSerie(materielRequest.getNumeroSerie());
        videoProjecteur.setNom(materielRequest.getNom());
        videoProjecteur.setModele(materielRequest.getModele());
        videoProjecteur.setMarque(materielRequest.getMarque());
        videoProjecteur.setDataAjout(materielRequest.getDateAjout());
        videoProjecteur.setDateFinGarantie(materielRequest.getDateFinGarantie());
        videoProjecteur.setEtat(materielRequest.getEtat());

        return videoProjecteur;
    }
}