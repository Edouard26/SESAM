import type { ReactNode } from "react";

export type EntityCardStatus = {
    label: string;
    variant?: "success" | "primary" | "warning" | "danger" | "secondary";
};

export type EntityCardConfig<T> = {
    icon: ReactNode | ((item: T) => ReactNode);
    title: (item: T) => ReactNode;
    subtitle?: (item: T) => ReactNode;
    status?: (item: T) => EntityCardStatus | null | undefined;
    content?: (item: T) => ReactNode;
    footer?: (item: T) => ReactNode;
};

type EntityCardProps<T> = EntityCardConfig<T> & {
    item: T;
};

const statusClasses: Record<
    NonNullable<EntityCardStatus["variant"]>,
    string
> = {
    success: "bg-success-subtle text-success-emphasis",
    primary: "bg-primary-subtle text-primary-emphasis",
    warning: "bg-warning-subtle text-warning-emphasis",
    danger: "bg-danger-subtle text-danger-emphasis",
    secondary: "bg-secondary-subtle text-secondary-emphasis",
};

export default function EntityCard<T>({
                                          item,
                                          icon,
                                          title,
                                          subtitle,
                                          status,
                                          content,
                                          footer,
                                      }: EntityCardProps<T>) {
    const cardIcon = typeof icon === "function" ? icon(item) : icon;
    const cardStatus = status?.(item);
    const cardContent = content?.(item);
    const cardFooter = footer?.(item);

    return (
        <article className="card h-100 border border-primary-subtle rounded-3">
            <div className="card-body p-4 d-flex flex-column gap-3">
                <div className="d-flex align-items-center gap-3">
                    <div className="p-3 bg-primary-subtle text-primary rounded-3 d-flex align-items-center justify-content-center flex-shrink-0">
                        {cardIcon}
                    </div>

                    <div className="flex-grow-1 overflow-hidden">
                        <h3 className="h6 mb-0 fw-semibold text-primary">
                            {title(item)}
                        </h3>

                        {subtitle && (
                            <div className="small text-secondary mt-2">
                                {subtitle(item)}
                            </div>
                        )}
                    </div>
                </div>

                {cardStatus && (
                    <div>
                        <span
                            className={`badge rounded-pill fw-normal ${
                                statusClasses[
                                cardStatus.variant ?? "secondary"
                                    ]
                            }`}
                        >
                            {cardStatus.label}
                        </span>
                    </div>
                )}

                {cardContent != null && (
                    <div className="small d-flex flex-column gap-3">
                        {cardContent}
                    </div>
                )}

                {cardFooter != null && (
                    <div className="border-top pt-3 mt-auto">
                        {cardFooter}
                    </div>
                )}
            </div>
        </article>
    );
}