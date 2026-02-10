import API_SERVER from "@/apiServer";
import Button from "@/components/utilities/Button";
import axios from "axios";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useRef, useState, useEffect } from "react";

export default function FeeVerificationRequest({ name }: { name: string | undefined }) {
    const router = useRouter();
    const defaultImage = "/images/icons/others/landscape_2.svg";
    const [imagePath, setImagePath] = useState<string>(defaultImage);
    const imageInputRef = useRef<HTMLInputElement>(null);
    const prevObjectUrlRef = useRef<string | null>(null);

    const pictureSelectOnClick = () => {
        imageInputRef.current?.click();
    };

    const inputOnChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const f = e.target.files?.[0];
        if (!f) return;

        // revoke previous object URL if exists
        if (prevObjectUrlRef.current) {
            URL.revokeObjectURL(prevObjectUrlRef.current);
            prevObjectUrlRef.current = null;
        }

        const url = URL.createObjectURL(f);
        prevObjectUrlRef.current = url;
        setImagePath(url);
    };

    // cleanup on unmount
    useEffect(() => {
        return () => {
            if (prevObjectUrlRef.current) {
                URL.revokeObjectURL(prevObjectUrlRef.current);
                prevObjectUrlRef.current = null;
            }
        };
    }, []);

    const upload = async (e: React.MouseEvent<HTMLButtonElement>) => {
        const f = imageInputRef.current?.files?.[0];
        if (!f) return;

        try {
            const fd = new FormData();
            fd.append("verificationImage", f);
            await axios.post(`${API_SERVER}/student-council-fee-verification`, fd, {
                headers: { "Content-Type": "multipart/form-data" },
                withCredentials: true,
            });
            alert("등록이 완료됐습니다. 처리후 카카오톡으로 안내해드리겠습니다.");
            router.back();
        } catch (err) {
            console.error(err);
            alert("서버의 문제입니다. 이문제가 계속될 경우 관리자에게 연락해주세요.");
        }
    };

    return (
        <div>
            <p>{name}님의 이번학기 학생회비 납부여부가 확인되지 않았습니다.</p>
            <div
                className={`w-full h-75 mt-3 border-boxBorder border rounded flex justify-center items-center cursor-pointer`}
                onClick={pictureSelectOnClick}
                style={
                    imagePath && imagePath !== defaultImage
                        ? {
                              backgroundImage: `url(${imagePath})`,
                              backgroundSize: "contain",
                              backgroundPosition: "center",
                              backgroundRepeat: "no-repeat",
                              backgroundColor: "white",
                          }
                        : { backgroundColor: "white" }
                }
            >
                {imagePath == defaultImage ? (
                    <div className="flex flex-col items-center">
                        <Image src={defaultImage} width={108} height={108} alt="사진선택 사진" />
                        <p className="text-placeholder mt-1">학생회비 납부사진 선택</p>
                    </div>
                ) : null}
            </div>
            <Button
                title="학생회비 납부내역 증명"
                className="mt-3 py-3.5 w-full"
                disabled={imagePath == defaultImage}
                onClick={upload}
            />
            <input type="file" className="hidden" ref={imageInputRef} onChange={inputOnChange} />
        </div>
    );
}
