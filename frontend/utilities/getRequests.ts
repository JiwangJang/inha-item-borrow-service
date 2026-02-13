import API_SERVER from "@/apiServer";
import RequestInterface, { RequestState, RequestType } from "@/types/RequestInterface";
import axios from "axios";
import { cookies } from "next/headers";

export default async function getRequests({
    borrowId,
    type,
    state,
}: {
    borrowId?: string | null;
    type?: RequestType;
    state?: RequestState;
} = {}): Promise<RequestInterface[]> {
    try {
        const params = new URLSearchParams();
        if (borrowId) params.append("borrowId", borrowId);
        if (type) params.append("type", type);
        if (state) params.append("state", state);

        const cookieStore = await cookies();
        const cookie = cookieStore.toString();
        const res = await fetch(`${API_SERVER}/requests?${params.toString()}`, {
            method: "GET",
            headers: {
                cookie,
            },
        });

        if (!res.ok) return [];

        const body = await res.json();

        return body.data;
    } catch (error) {
        console.error(error);
        return [];
    }
}
