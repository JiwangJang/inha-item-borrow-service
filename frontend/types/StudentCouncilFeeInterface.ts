export default interface StudentCouncilFeeInterface {
    id: string;
    verify: boolean | null;
    s3Link: string;
    requestAt: Date;
    responseAt: Date;
    denyReason: string | null;
}
