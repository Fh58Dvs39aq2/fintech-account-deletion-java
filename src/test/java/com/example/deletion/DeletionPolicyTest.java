package com.example.deletion;

public final class DeletionPolicyTest {
    public static void main(String[] args) {
        DeletionPolicy policy = new DeletionPolicy();
        assert !policy.evaluate("user_1", false, 2).approved();
        DeletionPolicy.Decision approved = policy.evaluate("user_1", true, 2);
        assert approved.approved();
        assert approved.reason().contains("2 sessions");
        System.out.println("DeletionPolicyTest passed");
    }
}
