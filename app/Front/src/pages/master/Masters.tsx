
import { useEffect, useState } from "react";
import { masterApi, processApi } from "../../api";
import type { Master, MasterForm } from "../../types/Master.ts";
import type { Processus } from "../../types/Processus.ts";
import useNotification from "../../hooks/useNotification.ts";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, { type Column } from "../../components/generic/display/Table.tsx";
import MasterPopup from "../../components/master/MasterPopup.tsx";
import MasterDeployPopup from "../../components/master/MasterDeployPopup.tsx";

// Liste des masters avec leurs processus associés, leur création et leur plaquage dans une salle
export default function Masters() {
    const { notify } = useNotification();

    const [processes, setProcesses] = useState<Processus[]>([]);
    const [creating, setCreating] = useState(false);
    const [saving, setSaving] = useState(false);
    const [masterToDeploy, setMasterToDeploy] = useState<Master | null>(null);

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
                    onClick={() => setMasterToDeploy(master)}
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

            <Table<Master>
                columns={columns}
                data={masters}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />

            {masterToDeploy && (
                <MasterDeployPopup
                    master={masterToDeploy}
                    onClose={() => setMasterToDeploy(null)}
                    onSubmit={() => setMasterToDeploy(null)}
                />
            )}

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
