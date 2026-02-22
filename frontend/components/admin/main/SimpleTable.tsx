type SimpleTableProps = {
    headers: string[];
    rows: (string | number)[][];
};

export default function SimpleTable({ headers, rows }: SimpleTableProps) {
    return (
        <table className="w-full border-t-2 border-b-2 bg-white border-black border-collapse text-center table-fixed">
            <colgroup>
                {headers.map((_, idx) => (
                    <col key={idx} style={{ width: idx === 0 ? "100px" : "auto" }} />
                ))}
            </colgroup>
            <thead>
                <tr className="bg-gray-100">
                    {headers.map((header, idx) => (
                        <th
                            key={idx}
                            className="border border-gray-300 py-2 bold-16px first:border-l-0 last:border-r-0"
                        >
                            {header}
                        </th>
                    ))}
                </tr>
            </thead>

            <tbody>
                {rows.map((row, rowIdx) => (
                    <tr key={rowIdx}>
                        {row.map((cell, cellIdx) => (
                            <td
                                key={cellIdx}
                                className="border border-gray-200 py-2 text-16px first:border-l-0 last:border-r-0"
                            >
                                {cell}
                            </td>
                        ))}
                    </tr>
                ))}
            </tbody>
        </table>
    );
}
