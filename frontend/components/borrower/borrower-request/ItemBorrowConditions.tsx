export default function ItemBorrowConditions() {
    return (
        <div className="bg-white border border-boxBorder rounded p-4">
            <p className="bold-16px mb-2">1. 대여자는 아래 식에 따른 보증금을 아래의 계좌로 납부하여야 합니다.</p>
            <ul className="list-disc pl-5">
                <li>보증금 = 물품가액 X 0.3(천원단위 내림)</li>
                <li>
                    위 식에 따른 보증금을{" "}
                    <span
                        className="text-blue-500 underline cursor-pointer"
                        onClick={() => {
                            navigator.clipboard.writeText("카카오뱅크 0000-0000 0000");
                            alert("클립보드에 복사되었습니다.");
                        }}
                    >
                        카카오뱅크 0000-0000 0000
                    </span>
                    계좌로 하루 이내로 계좌로 하루 이내로 입금해주세요.(입금자명과 대여자명이 동일해야함)
                </li>
            </ul>
            <p className="bold-16px mb-2 mt-3">
                2. 납부하신 보증금은 물품 반납후 8일이내에 반환해 드립니다. 단, 아래의 경우에 해당하는 경우 그렇지
                않습니다.
            </p>
            <ul className="list-disc pl-5">
                <li>학생회의 귀책사유 없이 반납기한을 준수하지 않은 경우, 보증금의 반액을 감하여 환급합니다.</li>
                <li>
                    물품 파손 시 보증금을 반환하지 않으며, 이에 더해 대여자는 물품가액의 전액에서 보증금을 공제한 액수를
                    변상합니다. 변상금 전액이 입금될 때까지 해당 대여자는 대여사업 이용이 금지됩니다.
                </li>
            </ul>
        </div>
    );
}
