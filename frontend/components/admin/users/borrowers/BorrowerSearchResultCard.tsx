import { useRouter } from "next/navigation";

export default function BorrowerSearchResultCard({
    id,
    name,
    department,
    ban,
}: {
    id: string;
    name: string;
    department: string;
    ban: boolean;
}) {
    const router = useRouter();
    return (
        <div
            className="bg-white border border-boxBorder rounded-xl py-4 px-5 cursor-pointer"
            onClick={() => router.push(`/admin/users/borrowers/${id}`)}
        >
            <p>{department}</p>
            <div className="flex justify-between">
                <p className="bold-16px">
                    {name}({id})
                </p>
                <p className="regular-14px text-placeholder">{ban ? "이용금지" : "이용가능"}</p>
            </div>
        </div>
    );
}
