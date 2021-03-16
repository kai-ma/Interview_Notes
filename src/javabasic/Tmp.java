package javabasic;

public class Tmp {
    public boolean searchMatrix(int[][] matrix, int target) {
        if (matrix.length == 0 || matrix[0].length == 0) {
            return false;
        }
        int r = matrix.length;
        int c = matrix[0].length;
        int left = 0;
        int right = r * c - 1;
        while (left != right) {
            int mid = left + (right - left) / 2;
            if (target > matrix[mid / c][mid % c]) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return matrix[left / c][left % c] == target;
    }


    public static void main(String[] args) {
        System.out.println(-9 % 10);
    }
}
