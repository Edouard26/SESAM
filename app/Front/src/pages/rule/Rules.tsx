import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { processApi, ruleApi } from "../../api";
import type { Processus } from "../../types/Processus.ts";
import type { Rule } from "../../types/Rule.ts";
import type { RuleForm } from "../../types/RuleForm.ts";
import { Role } from "../../types/Role.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import useNotification from "../../hooks/useNotification.ts";
import usePaginatedList from "../../hooks/usePaginatedList.ts";
import PageHeader from "../../components/layout/PageHeader.tsx";
import Filters from "../../components/generic/Filters.tsx";
import Table, { type Column } from "../../components/generic/display/Table.tsx";
import RuleFormPopup from "../../components/rule/RuleFormPopup.tsx";
import Confirmation from "../../components/generic/overlay/Confirmation.tsx";

// Gestion des règles (création, modification, suppression) ; accès réservé au rôle FORMATEUR et au-dessus
export default function Rules() {
    const { user } = useAuth();
    const { notify } = useNotification();
    const [processes, setProcesses] = useState<Processus[]>([]);
    const [editing, setEditing] = useState<Rule | null | undefined>(undefined);
    const [saving, setSaving] = useState(false);

    const {
        search, items: rules, loading, page, totalPages, totalElements,
        setPage, setSearch, reload,
    } = usePaginatedList({
        load: ruleApi.list,
        initialFilters: {},
        pageSize: 10,
        errorMessage: "Impossible de charger les règles.",
        initialSort: "nom,asc",
    });

    useEffect(() => {
        let cancelled = false;
        processApi.list({ page: 0, size: 1000, sort: "nom,asc" })
            .then((result) => { if (!cancelled) setProcesses(result.content); })
            .catch(() => { if (!cancelled) notify("Impossible de charger les processus.", "danger"); });
        return () => { cancelled = true; };
    }, [notify]);

    // Crée la règle, ou la met à jour si une règle est en cours d'édition
    async function saveRule(form: RuleForm) {
        setSaving(true);
        try {
            if (editing) {
                await ruleApi.update(editing.id, form);
                notify("Règle mise à jour.", "success");
            } else {
                await ruleApi.create(form);
                notify("Règle créée.", "success");
            }
            setEditing(undefined);
            reload();
        } catch {
            notify("Impossible d’enregistrer la règle.", "danger");
        } finally {
            setSaving(false);
        }
    }

    async function deleteRule(rule: Rule) {
        try {
            await ruleApi.delete(rule.id);
            notify("Règle supprimée et détachée des salles.", "success");
            reload();
        } catch {
            notify("Impossible de supprimer la règle.", "danger");
        }
    }

    // Placé après les hooks : les redirections ne doivent pas changer leur ordre d'appel
    if (!user || user.role < Role.FORMATEUR) return <Navigate to="/dashboard" replace />;

    const columns = [
        { key: "nom", label: "Règle" },
        {
            label: "Processus interdits",
            render: (rule: Rule) => rule.processusId.length
                ? rule.processusId.map((id) => processes.find((process) => process.id === id)?.nom ?? `#${id}`).join(", ")
                : <span className="text-body-secondary">Aucun processus</span>,
        },
        {
            label: "Actions",
            render: (rule: Rule) => (
                <div className="d-flex gap-2">
                    <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => setEditing(rule)}>Modifier</button>
                    <Confirmation
                        title="Supprimer la règle ?"
                        message="Elle sera également retirée de toutes les salles qui lui sont associées."
                        onConfirm={() => void deleteRule(rule)}
                        trigger={<button type="button" className="btn btn-sm btn-outline-danger">Supprimer</button>}
                    />
                </div>
            ),
        },
    ] satisfies Column<Rule>[];

    return (
        <>
            <PageHeader
                title="Règles"
                description="Gérer les processus interdits dans les salles."
                actions={[{ label: "Ajouter une règle", icon: "bi-plus-lg", variant: "primary", onClick: () => setEditing(null) }]}
            />
            <Filters
                search={search}
                searchPlaceholder="Rechercher une règle ou un processus..."
                values={{}}
                onSearchChange={setSearch}
                onFilterChange={() => {}}
                onReset={() => setSearch("")}
            />
            <Table
                columns={columns}
                data={rules}
                currentPage={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
                loading={loading}
            />
            {editing !== undefined && (
                <RuleFormPopup
                    rule={editing}
                    processes={processes}
                    saving={saving}
                    onClose={() => setEditing(undefined)}
                    onSubmit={(form) => void saveRule(form)}
                />
            )}
        </>
    );
}
