export default interface StudentCouncilFeeVerificationInterface {
    id: string;
    verify: boolean | null;
    s3Link: string;
    requestAt: string;
    responseAt: string;
    denyReason: string | null;
}
