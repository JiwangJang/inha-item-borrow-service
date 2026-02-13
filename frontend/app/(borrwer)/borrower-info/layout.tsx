import checkLogin from "@/utilities/checkLogin";
import { redirect } from "next/navigation";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const result = await checkLogin();
    if (result == null) {
        redirect("/login");
    }

    return <div>{children}</div>;
}
