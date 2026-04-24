package Walmart.Round1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integer array

 * fn ->
 * 1.sum of values of this array ->
 * 2.print reverse values of the sum
 * 3.Array of the sum digits
 *
 *  * Input ->    [9,8,7,6]
 *  *           + [6,7,8,9]
 *    Output ->  [1,6,6,6,5]
 *16
 *    1. reverse O(n/2) -> swapping -> store it
 *    2. sum (num1, num2) -> carry
 *    Tc -> o(n) , sc O(N)
 */
public class Solution {
    static int[] solve(int[] arr) {
        int n = arr.length;
        int[] reverse = reverseArray(arr, n);
        System.out.println(Arrays.toString(reverse));
        List<Integer> list = new ArrayList<>();
        int carry = 0;
        for(int i=n-1;i>=0;i--) {
            int sum = arr[i] + reverse[i] + carry;
            int digit = sum%10;
            carry = sum/10;
            list.addFirst(digit);
        }
        if(carry!=0) {
            list.addFirst(carry);
        }
        System.out.println(list);
        return new int[]{};
    }

    static int[] reverseArray(int[] arr, int n) {
        int[] ans = new int[n];
        for(int i=0;i<n;i++) {
            ans[i] = arr[n-1-i];
        }
        return ans;
    }

    static void main() {
        int[] num = {9,8,1,0};
        //           0 1 8 9
        solve(num);
    }

}
