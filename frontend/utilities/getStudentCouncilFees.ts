"use server";

import API_SERVER from "@/apiServer";
import StudentCouncilFeeInterface from "@/types/StudentCouncilFeeInterface";
import axios from "axios";
import { cookies } from "next/headers";

export default async function getStudentCouncilFees(): Promise<StudentCouncilFeeInterface[]> {
    try {
        const cookieStore = await cookies();
        const cookie = cookieStore.toString();
        const res = await fetch(`${API_SERVER}/student-council-fee-verification`, {
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
