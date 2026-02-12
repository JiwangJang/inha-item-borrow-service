import AdminLoginPage from "@/components/admin/admin-login/AdminLoginPage";
import checkAdminLogin from "@/utilities/checkAdminLogin";
import { redirect } from "next/navigation";

export default async function page() {
    const adminInfo = await checkAdminLogin();
    if (adminInfo != null) {
        redirect("/admin");
    }

    return (
        <div className="w-full h-screen bg-back pt-15 px-6">
            <AdminLoginPage />
        </div>
    );
}
