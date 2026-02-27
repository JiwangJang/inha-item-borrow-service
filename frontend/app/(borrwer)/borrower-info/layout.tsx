import checkBorrowLogin from "@/utilities/checkBorrowLogin";
import { redirect } from "next/navigation";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const result = await checkBorrowLogin();

    if (result == null) {
        redirect("/login");
    }

    return <div>{children}</div>;
}
