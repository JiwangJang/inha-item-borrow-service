import API_SERVER from "@/apiServer";
import { cookies } from "next/headers";

export default async function getItemInfo(id: string) {
    try {
        const cookieStore = await cookies();
        const cookie = cookieStore.toString();

        const res = await fetch(`${API_SERVER}/items/${id}`, {
            method: "GET",
            headers: {
                cookie,
            },
            cache: "no-store",
        });
        const data = await res.json();

        if (!res.ok) {
            return data.errorMsg;
        }

        return data.data;
    } catch (error) {
        console.log(error);
    }
}
