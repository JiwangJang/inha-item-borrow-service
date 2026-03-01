"use server";

import API_SERVER from "@/apiServer";
import { cookies } from "next/headers";

export default async function getItems() {
    try {
        const cookieStore = await cookies();
        const cookie = cookieStore.toString();

        const res = await fetch(`${API_SERVER}/items`, {
            method: "GET",
            headers: {
                cookie,
            },
            cache: "no-cache",
        });

        if (!res.ok) return [];

        const result = await res.json();
        return result.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}
