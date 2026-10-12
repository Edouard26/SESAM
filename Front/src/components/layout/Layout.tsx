import { Outlet } from "react-router-dom";
import Header from "./Header";
import Sidebar from "./Sidebar";

export default function Layout() {
    return (
        <div className="d-flex min-vh-100">
            <Sidebar />

            <div className="content flex-grow-1">
                <Header />

                <main className="p-4">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}