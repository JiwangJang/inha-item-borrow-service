import Button from "@/components/utilities/Button";
import { useRouter } from "next/navigation";
import { useRef, useState, useEffect } from "react";
import axios from "axios";
import API_SERVER from "@/apiServer";
import StudentCouncilFeeVerificationInterface from "@/types/StudentCouncilFeeVerificationInterface";
import S3_URL from "@/utilities/s3URL";

export default function FeeVerificationStatus({
    verification,
    name,
}: {
    verification: StudentCouncilFeeVerificationInterface;
    name: string | undefined;
}) {
    const router = useRouter();
    const [preview, setPreview] = useState<string>(S3_URL + verification.s3Link);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const imageInputRef = useRef<HTMLInputElement | null>(null);
    const prevObjectUrlRef = useRef<string | null>(null);

    const isReviewing = !verification.verify && verification.responseAt == null;
    const isReject = !verification.verify && verification.responseAt != null;

    useEffect(() => {
        return () => {
            if (prevObjectUrlRef.current) {
                URL.revokeObjectURL(prevObjectUrlRef.current);
                prevObjectUrlRef.current = null;
            }
        };
    }, []);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const f = e.target.files?.[0] ?? null;
        if (!f) return;

        if (prevObjectUrlRef.current) {
            URL.revokeObjectURL(prevObjectUrlRef.current);
            prevObjectUrlRef.current = null;
        }

        const url = URL.createObjectURL(f);
        prevObjectUrlRef.current = url;
        setPreview(url);
        setSelectedFile(f);
    };

    const handleUpload = async () => {
        if (!selectedFile) return;

        try {
            const fd = new FormData();
            fd.append("verificationImage", selectedFile);
            await axios.post(`${API_SERVER}/student-council-fee-verification`, fd, {
                headers: { "Content-Type": "multipart/form-data" },
                withCredentials: true,
            });
            alert("재제출이 완료됐습니다.");

            router.back();
        } catch (err) {
            console.error(err);
            alert("서버에 업로드 중 오류가 발생했습니다.");
        }
    };

    return (
        <div className="mt-1">
            <p className="mb-3">
                {verification.verify
                    ? `${name}님의 이번학기 학생회비 납부여부를 확인하였습니다.`
                    : isReviewing
                      ? `${name}님의 이번 학기 학생회비 납부여부를 확인중입니다.`
                      : `아래 사유로 인해 ${name}님의 이번 학기 학생회비 납부여부를 확인하지 못했습니다. 다시 제출해주시기 바랍니다`}
            </p>
            <div
                className="w-full h-75 border border-boxBorder cursor-pointer"
                style={{
                    backgroundImage: preview ? `url(${preview})` : undefined,
                    backgroundSize: "contain",
                    backgroundPosition: "center",
                    backgroundRepeat: "no-repeat",
                    backgroundColor: "white",
                }}
                onClick={() => {
                    if (isReject) {
                        imageInputRef.current?.click();
                    } else if (verification.s3Link) {
                        window.open(S3_URL + verification.s3Link);
                    }
                }}
            />
            {isReviewing ? null : (
                <p className="mt-2 text-right"> 확인일시 : {verification.responseAt?.replace("T", " ")}</p>
            )}
            {!verification.verify && verification.responseAt != null ? (
                <>
                    <Button
                        title="재제출"
                        className="py-3 w-full mt-3"
                        disabled={!selectedFile}
                        onClick={() => handleUpload()}
                    />
                    <p className="mt-2">실패사유 : {verification.denyReason}</p>
                    <input type="file" className="hidden" ref={imageInputRef} onChange={handleFileChange} />
                </>
            ) : null}
        </div>
    );
}
