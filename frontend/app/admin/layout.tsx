import AdminItemProvider from "@/components/provider/AdminItemProvider";
import AdminListProvider from "@/components/provider/AdminListProvider";
import AdminProvider from "@/components/provider/AdminProvider";
import AdminRequestProvider from "@/components/provider/AdminRequestProvider";
import AdminStudentCouncilFeeProvider from "@/components/provider/AdminStudentCouncilFeeProvider";
import SearchedBorrowersProvider from "@/components/provider/SearchedBorrowersProvider";
import checkAdminLogin from "@/utilities/checkAdminLogin";
import getAdminList from "@/utilities/getAdminList";
import getItems from "@/utilities/getItems";
import getRequests from "@/utilities/getRequests";
import getStudentCouncilFees from "@/utilities/getStudentCouncilFees";
import { redirect } from "next/navigation";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const adminInfo = await checkAdminLogin();

    if (adminInfo == null) {
        redirect("/admin-login");
    }

    const itemList = await getItems();
    const requestList = await getRequests();
    const studentCouncilList = await getStudentCouncilFees();
    const adminList = await getAdminList();

    return (
        <div className="w-full pt-15 pb-16 bg-back min-h-screen common-px">
            <AdminProvider initialValue={adminInfo}>
                <AdminRequestProvider initialValue={requestList}>
                    <AdminStudentCouncilFeeProvider initialValue={studentCouncilList}>
                        <AdminItemProvider initialValue={itemList}>
                            <AdminListProvider initialValue={adminList}>
                                <SearchedBorrowersProvider>{children}</SearchedBorrowersProvider>
                            </AdminListProvider>
                        </AdminItemProvider>
                    </AdminStudentCouncilFeeProvider>
                </AdminRequestProvider>
            </AdminProvider>
        </div>
    );
}
