type BadgeProps = {
    children: React.ReactNode;
    variant?: string;
};

export default function Badge({ children, variant = "primary" }: BadgeProps) {
    return (
        <span className={`badge text-bg-${variant}`}>
            {children}
        </span>
    );
}