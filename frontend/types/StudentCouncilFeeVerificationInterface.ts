export default interface StudentCouncilFeeVerificationInterface {
    id: number;
    borrowerId: string;
    borrowerName: string;
    verify: boolean | null;
    s3Link: string | null;
    requestAt: string | null;
    responseAt: string | null;
    denyReason: string | null;
}
