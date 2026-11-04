import { createContext, useCallback, useState } from "react";
import Notification from "./Notification";

type NotificationVariant =
    | "success"
    | "danger"
    | "warning"
    | "info";

type NotificationItem = {
    id: number;
    message: string;
    variant: NotificationVariant;
    duration: number;
};

type NotificationContextType = {
    notify: (
        message: string,
        variant?: NotificationVariant,
        duration?: number
    ) => void;
};

export const NotificationContext =
    createContext<NotificationContextType | null>(null);

let nextNotificationId = 0;

type NotificationProviderProps = {
    children: React.ReactNode;
};

// Fournit notify() pour afficher des notifications temporaires empilées en bas à droite de l'écran
export default function NotificationProvider({ children }: NotificationProviderProps) {
    const [notifications, setNotifications] = useState<NotificationItem[]>([]);

    const notify = useCallback(
        (
            message: string,
            variant: NotificationVariant = "info",
            duration: number = 5000
        ) => {
            const id = ++nextNotificationId;

            setNotifications(current => [
                ...current,
                { id, message, variant, duration }
            ]);

            // Suppression automatique à la fin de la durée
            setTimeout(() => {
                setNotifications(current => current.filter(notification => notification.id !== id));
            }, duration);
        },
        []
    );

    const removeNotification = (id: number) => {
        setNotifications(current =>
            current.filter(notification => notification.id !== id)
        );
    };

    return (
        <NotificationContext.Provider value={{ notify }}>
            {children}

            <div className="position-fixed bottom-0 end-0 p-3 d-flex flex-column gap-2"
                style={{
                    zIndex: 1055,
                    width: "350px"
                }}
            >
                {notifications.map(notification => (
                    <Notification key={notification.id} message={notification.message} variant={notification.variant} duration={notification.duration}
                                  onClose={() => removeNotification(notification.id)}/>
                ))}
            </div>
        </NotificationContext.Provider>
    );
}