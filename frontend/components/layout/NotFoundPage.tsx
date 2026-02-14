"use client";

import Image from "next/image";
import { usePathname, useRouter } from "next/navigation";
import Button from "../utilities/Button";

export default function NotFoundPage() {
    const router = useRouter();
    const pathname = usePathname();

    return (
        <div className="h-screen bg-back pt-3.5 px-5">
            <div className="border-2 border-boxBorder mt-5 rounded-3xl overflow-hidden">
                <div
                    className="w-full h-56 "
                    style={{
                        backgroundColor: "white",
                        backgroundImage: "url(/images/not-found.png)",
                        backgroundPosition: "center",
                        backgroundRepeat: "no-repeat",
                        backgroundSize: "contain",
                    }}
                ></div>
                <div className="py-4 bold-18px text-center bg-white">
                    <p>요청하신 경로는 존재하지 않는 경로입니다.</p>
                    <p>요청경로 : {pathname}</p>
                </div>
            </div>
            <Button title="돌아가기" className="w-full mt-3 py-3 bold-18px" onClick={() => router.back()} />
        </div>
    );
}
