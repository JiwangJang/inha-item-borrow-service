import Image from "next/image";
import Link from "next/link";

export default function V1AgreementSection() {
    return (
        <div className="bg-white border border-boxBorder rounded py-5 px-4">
            <p className="text-center black-20px">미래융합대학 물품대여서비스 개인정보처리방침</p>
            <div className="my-4 leading-snug">
                제4대 미래융합대학 학생회는 정보주체의 자유와 권리 보호를 위해 ｢개인정보 보호법｣ 및 관계 법령이정한 바를
                준수하여, 적법하게 개인정보를 처리하고 안전하게 관리하고 있습니다. 이에 ｢개인정보 보호법｣ 제30조에 따라
                정보주체에게 개인정보의 처리와 보호에 관한 절차 및 기준을 안내하고, 이와 관련한 고충을 신속하고 원활하게
                처리할 수 있도록 하기 위하여 다음과 같이 개인정보 처리방침을 수립·공개합니다.
            </div>
            {/* 개인정보 처리방침 요약본 */}
            <div className="flex flex-col gap-4">
                <PrivacyContent title="개인정보 처리항목 및 처리목적" imageFileName="individual.png">
                    <table
                        className="w-full bg-white border border-black border-collapse
                            [&_th]:border 
                            [&_th]:border-gray-black 
                            [&_td]:border 
                            [&_td]:border-gray-black 
                            [&_td]:p-2"
                    >
                        <thead className="bg-gray-100">
                            <tr>
                                <th className="w-30">수집정보명</th>
                                <th>처리목적</th>
                            </tr>
                        </thead>
                        <tbody className="text-center">
                            <tr>
                                <td>학번, 이름, 학과</td>
                                <td>사업 진행간 물품 대여기록 유지</td>
                            </tr>
                            <tr>
                                <td>
                                    핸드폰번호 <br />
                                    계좌번호
                                </td>
                                <td>사업 진행간 물품 대여기록 유지</td>
                            </tr>
                            <tr>
                                <td>
                                    학생회비
                                    <br /> 납부사진
                                </td>
                                <td>사업 진행간 물품 대여기록 유지</td>
                            </tr>
                        </tbody>
                    </table>
                </PrivacyContent>
                <PrivacyContent title="보유기간 및 파기" imageFileName="period.png">
                    <p className="text-center">
                        각 개인정보는 본 개인정보 처리방침 동의일로부터 물품대여사업 대상 감사 종료시까지 보유합니다.
                    </p>
                </PrivacyContent>
                <PrivacyContent title="개인정보 고충처리부서" imageFileName="counselor.png">
                    <div className="px-2 bold-14px">
                        <p className="">🏢 | 제4대 미래융합대학 학생회</p>
                        <p className="">☎️ | 010-8387-7834</p>
                        <p className="">✉️ | inhafuture.4th@gmail.com</p>
                    </div>
                </PrivacyContent>
            </div>
            {/* 최초작성일 */}
            <div className="my-4 text-center bold-18px">최초작성일 : 2026. 3. 1.</div>
            {/* 세부내역 문서 링크(구글드라이브) */}
            <div className="my-4 leading-snug text-center">
                자세한 사항은{" "}
                <Link href={""} className="underline text-blue-600">
                    개인정보 처리방침 세부내역(링크)
                </Link>{" "}
                에서 확인가능합니다.
            </div>
        </div>
    );
}

function PrivacyContent({
    imageFileName,
    title,
    children,
}: {
    imageFileName: string;
    title: string;
    children: React.ReactNode;
}) {
    return (
        <div className="border border-indigo-200 bg-indigo-50 rounded-xl overflow-hidden pb-3">
            <div className="h-30 relative bg-white">
                <Image src={`/images/privacy/${imageFileName}`} fill objectFit="contain" alt="개인정보 이미지" />
            </div>
            <p className="text-center bold-16px my-2">{title}</p>
            <div className="px-1 text-[14px]">{children}</div>
        </div>
    );
}
