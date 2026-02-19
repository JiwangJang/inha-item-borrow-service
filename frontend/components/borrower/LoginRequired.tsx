"use client";

import { useRouter } from "next/navigation";
import Button from "../utilities/Button";

export default function LoginRequired() {
    const router = useRouter();

    return (
        <div className="pt-3.5">
            <div className="border-2 border-boxBorder mt-5 rounded-3xl overflow-hidden">
                <div
                    className="w-full h-56"
                    style={{
                        backgroundColor: "white",
                        backgroundImage: "url(/images/need-login.png)",
                        backgroundPosition: "center",
                        backgroundRepeat: "no-repeat",
                        backgroundSize: "contain",
                    }}
                ></div>
                <div className="py-4 bold-18px text-center bg-white">
                    <p>요청하신 경로는 로그인이 필요한 경로입니다.</p>
                </div>
            </div>
            <Button
                title="로그인하러가기"
                className="w-full mt-5 py-3 bold-18px"
                onClick={() => router.push("/login")}
            />
            <Button
                title="돌아가기"
                className="w-full mt-2 py-3 bold-18px bg-placeholder!"
                onClick={() => router.back()}
            />
        </div>
    );
}
