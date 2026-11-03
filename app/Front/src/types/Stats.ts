export type Stats = {
    materiels: {
        total: number;
        parType: Record<string, number>;
        parEtat: Record<string, number>;
        garantieExpiree: number;
        garantieExpirantSous30Jours: number;
    };
    salles: {
        total: number;
        salleCours: number;
        salleStockage: number;
        parEtat: Record<string, number>;
    };
    alertes: {
        total: number;
        parStatut: Record<string, number>;
    };
    stages: {
        total: number;
        parStatut: Record<string, number>;
    };
};
