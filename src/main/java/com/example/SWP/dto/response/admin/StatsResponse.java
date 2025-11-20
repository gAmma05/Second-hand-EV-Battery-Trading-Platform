package com.example.SWP.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class StatsResponse {
    int totalUser;
    int totalOnlineUser;

    int totalSeller;
    int totalBuyer;

    int totalPost;
    int totalPendingPost;
    int totalDeletedPost;
    int totalPostedPost;

    int totalOrder;
    int totalPendingOrder;
    int totalApprovedOrder;
    int totalRejectedOrder;
    int totalDoneOrder;

    int totalFeedback;

    int totalContract;
    int totalPendingContract;
    int totalCancelledContract;
    int totalSignedContract;

    int totalComplaint;
    int totalPendingComplaint;
    int totalResolvedComplaint;

    BigDecimal totalRevenue;

}
