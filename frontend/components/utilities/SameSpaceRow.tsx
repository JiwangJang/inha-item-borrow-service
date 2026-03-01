export default function SameSpaceRow({
    label,
    value,
    labelWidth = 60,
}: {
    label: string;
    value: string;
    labelWidth?: number;
}) {
    function splitCharacters(text: string): string[] {
        // Array.from handles Unicode properly (including Korean characters)
        return Array.from(text);
    }

    return (
        <div className="flex">
            {/* Stretch the label text so the colon aligns nicely (Korean-friendly) */}
            <div
                className="flex justify-between"
                style={{
                    width: labelWidth,
                }}
            >
                {splitCharacters(label).map((s, i) => (
                    <span key={i}>{s}</span>
                ))}
            </div>
            <span className="mx-1">:</span>
            <span>{value}</span>
        </div>
    );
}
