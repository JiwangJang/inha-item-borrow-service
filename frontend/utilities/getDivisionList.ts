import API_SERVER from "@/apiServer";
import { cookies } from "next/headers";

export default async function getDivisionList() {
    try {
        const cookieStore = await cookies();
        const cookie = cookieStore.toString();
        const res = await fetch(`${API_SERVER}/divisions`, { method: "GET", headers: { cookie } });
        const json = await res.json();

        if (!res.ok) return [];

        return json.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}
