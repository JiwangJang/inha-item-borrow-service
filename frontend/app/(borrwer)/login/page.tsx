import BorrowerLoginPage from "@/components/borrower/borrower-info/login/BorrowerLoginPage";
import checkLogin from "@/utilities/checkLogin";
import { redirect } from "next/navigation";

export default async function Page() {
    const loginResult = await checkLogin();

    if (loginResult != null) {
        redirect("/");
    }
    return <BorrowerLoginPage />;
}
