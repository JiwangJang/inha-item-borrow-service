"use server";

import API_SERVER from "@/apiServer";
import BorrowerInfoInterface from "@/types/BorrowerInfoInterface";
import { cookies } from "next/headers";

/**
 * 대여자 인증 정보 확인하는 함수
 */
export default async function checkLogin(): Promise<BorrowerInfoInterface | null> {
    try {
        const cookieStore = await cookies();
        const cookie = cookieStore.toString();

        // 서버컴포넌트에서는 fetch 사용
        const res = await fetch(`${API_SERVER}/borrowers/me`, {
            method: "GET",
            headers: {
                cookie,
            },
            cache: "no-store",
        });

        if (!res.ok) return null;
        const data = await res.json();
        return data.data;
    } catch (e) {
        console.log(e);
        return null;
    }
}
