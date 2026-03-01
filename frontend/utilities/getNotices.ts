"use server";

import API_SERVER from "@/apiServer";

export default async function getNotices() {
    try {
        const res = await fetch(`${API_SERVER}/notices`, { method: "GET", cache: "no-cache" });
        const result = await res.json();
        if (!res.ok) return [];

        return result.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}
