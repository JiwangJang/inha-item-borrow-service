import BorrowerLoginPage from "@/components/borrower/login/BorrowerLoginPage";
import checkLogin from "@/utilities/checkBorrowLogin";
import { redirect } from "next/navigation";

export default async function Page() {
    const loginResult = await checkLogin();

    if (loginResult != null) {
        redirect("/");
    }
    return <BorrowerLoginPage />;
}
