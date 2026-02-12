import AdminItemProvider from "@/components/provider/AdminItemProvider";
import AdminProvider from "@/components/provider/AdminProvider";
import checkAdminLogin from "@/utilities/checkAdminLogin";
import getItems from "@/utilities/getItems";
import { redirect } from "next/navigation";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const adminInfo = await checkAdminLogin();

    if (adminInfo == null) {
        redirect("/admin-login");
    }

    const itemList = await getItems();

    return (
        <div className="w-full pt-15 pb-16 bg-back min-h-screen common-px">
            <AdminProvider initialValue={adminInfo}>
                <AdminItemProvider initialValue={itemList}>{children}</AdminItemProvider>
            </AdminProvider>
        </div>
    );
}
