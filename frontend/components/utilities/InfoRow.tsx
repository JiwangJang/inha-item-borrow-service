export default function InfoRow({ label, value }: { label: string; value: string }) {
    return (
        <div className="grid grid-cols-[100px_1fr] border-b border-boxBorder last:border-b-0">
            <div className="bg-black text-white flex items-center justify-center px-4 py-4 bold-16px">{label}</div>

            <div className="bg-white flex items-center justify-center px-4 py-4 regular-16px text-slate-900">
                {value}
            </div>
        </div>
    );
}
