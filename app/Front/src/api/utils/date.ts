export function toIsoDate(value: Date | string): string {
    const date = value instanceof Date ? value : fromIsoDate(value);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${date.getFullYear()}-${month}-${day}`;
}

export function fromIsoDate(value: Date | string): Date {
    if (value instanceof Date) return value;
    return /^\d{4}-\d{2}-\d{2}$/.test(value) ? new Date(`${value}T00:00:00`) : new Date(value);
}
