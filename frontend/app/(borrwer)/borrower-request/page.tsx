import BorrowerRequestPage from "@/components/borrower/borrower-request/BorrowerRequestPage";
import checkBorrowLogin from "@/utilities/checkBorrowLogin";
import { redirect } from "next/navigation";

export default async function Page() {
    const result = await checkBorrowLogin();
    if (result == null) {
        redirect("/login");
    }
    return <BorrowerRequestPage />;
}
