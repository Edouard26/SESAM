
import { useEffect, useState } from "react";

type RequiredFieldMessageProps = {
    formId: string;
};

export default function RequiredFieldMessage({ formId }: RequiredFieldMessageProps) {
    const [invalidCount, setInvalidCount] = useState(0);

    useEffect(() => {
        const form = document.getElementById(formId) as HTMLFormElement | null;

        if (!form) return;

        const validate = () => {
            const fields = Array.from(
                form.querySelectorAll<
                    HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
                >(":required")
            );

            let count = 0;

            fields.forEach(field => {
                if (field.disabled) {
                    field.classList.remove("is-invalid");
                    return;
                }

                let invalid: boolean;

                if (
                    field instanceof HTMLInputElement &&
                    (field.type === "checkbox" || field.type === "radio")
                ) {
                    invalid = !field.checked;
                } else {
                    invalid = !field.value.trim();
                }

                field.classList.toggle("is-invalid", invalid);

                if (invalid) count++;
            });

            setInvalidCount(count);
        };

        const observer = new MutationObserver(validate);

        observer.observe(form, {
            childList: true,
            subtree: true,
            attributes: true,
            attributeFilter: ["value", "required", "disabled"]
        });

        form.addEventListener("input", validate);
        form.addEventListener("change", validate);

        validate();

        return () => {
            observer.disconnect();
            form.removeEventListener("input", validate);
            form.removeEventListener("change", validate);
        };
    }, [formId]);

    if (invalidCount === 0) return null;

    return (
        <div className="text-danger small mt-2" role="status">
            {invalidCount} champ(s) obligatoire(s) à remplir.
        </div>
    );
}
