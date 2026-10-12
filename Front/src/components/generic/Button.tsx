type ButtonProps = {
    children: React.ReactNode;
    onClick?: () => void;
    type?: "button" | "submit" | "reset";
    variant?: string;
    disabled?: boolean;
};

export default function Button({ children, onClick, type = "button", variant = "primary", disabled = false }: ButtonProps) {
    return (
        <button type={type} className={`btn btn-${variant}`} onClick={onClick} disabled={disabled}>
            {children}
        </button>
    );
}