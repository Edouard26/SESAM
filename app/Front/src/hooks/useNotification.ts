import { useContext } from "react";
import { NotificationContext } from "../components/generic/notification/NotificationProvider";

export default function useNotification() {
    const context = useContext(NotificationContext);

    if (!context) {
        throw new Error(
            "useNotification doit être utilisé dans NotificationProvider"
        );
    }

    return context;
}