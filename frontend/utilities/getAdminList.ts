import API_SERVER from "@/apiServer";
import AdminInfoInterface from "@/types/AdminInfoInterface";
import { cookies } from "next/headers";

export default async function getAdminList(): Promise<AdminInfoInterface[]> {
    try {
        const cookieStore = await cookies();
        const cookie = cookieStore.toString();

        const res = await fetch(`${API_SERVER}/admins`, { method: "GET", headers: { cookie } });
        if (!res.ok) {
            return [];
        }

        const adminList = (await res.json()).data;
        return adminList;
    } catch (error) {
        return [];
    }
}
