// 1. choose size n, 10 < n < 1000
// 2. each block 1...n gets assigned a probability req(i) (the probability that a process might request that block if it's free)
// 3. for each block 1...n a size(n), 1 < size(n) < 20, is assigned to that block which represents the number of pages that will be requested
// 4. for each i,j < n, metoo(i | j), the probability of i being requested if i hasn't already been allocated and j has been, is defined
// 5. only one req(), free(), or metoo() is called per time step of the simulation

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MemorySimulator {

    public static void main(String[] args) throws IOException {
        int n = 500; // size of memory partitions

        int[] pageList = new int[1000]; // 0 or 1 for if allocated or not

        ArrayList<MemoryPartition> partitions = new ArrayList<>(); // stores partitions

        // initialize n partitions
        for (int i = 0; i < n; i++) {
            partitions.add(new MemoryPartition(0, 0, 0, false));
        }

        generatePages(partitions); // generate pages of the memory partitions

        generateFree(partitions); // generate free probabilities

        generateReq(partitions); // generate req probabilities

        generateMeToo(partitions); // generate meToo probabilities for each partition

        Random rn = new Random();

        int jIndex = 0;
        int choice = 0;
        double[] fragmentationValues = new double[100000];
        // simulate 100000 time steps
        for (int t = 0; t < 100000; t++) {
            // Requesting simulation loop
            if (choice == 0) {
                for (int j = 0; j < partitions.size(); j++) {
                    MemoryPartition partition = partitions.get(j);
                    if (!partition.isAllocated()) {
                        if (roundNumber(rn.nextDouble()) < partition.getReq()) {
                            jIndex = j;
                            boolean isIthFitAllocated = ithFit(partition, pageList);
                            if (isIthFitAllocated) partition.setAllocated(true);
                            System.out.println("@t = " + t + " | REQUESTING partition " + (j + 1) + " | pages =  " + partition.getPages() + " | prob = " + partition.getReq());
                            break;
                        }
                    }
                }
            }


            // Freeing simulation loop
            if (choice == 1) {
                for (int j = 0; j < partitions.size(); j++) {
                    MemoryPartition partition = partitions.get(j);
                    if (partition.isAllocated()) {
                        if (roundNumber(rn.nextDouble()) < partition.getFree()) {
                            partition.setAllocated(false);
                            //System.out.println(partition.getPageIndexes());
                            for (int index : partition.getPageIndexes()) {
                                pageList[index] = 0;
                            }
                            partition.getPageIndexes().clear();
                            //System.out.println(partition.getPageIndexes());
                            System.out.println("@t = " + t + " | FREEING partition " + (j + 1) + " | pages =  " + partition.getPages() + " | prob = " + partition.getFree());
                            break;
                        }
                    }
                }
            }

            // MeToo simulation loop
            if (choice == 2) {
                for (int j = 0; j < partitions.size(); j++) {
                    MemoryPartition partition = partitions.get(j);
                    if (!partition.isAllocated()) {
                        if (roundNumber(rn.nextDouble()) < getMeToo(j, jIndex, partitions)) {
                            boolean isIthFitAllocated = ithFit(partition, pageList);
                            if (isIthFitAllocated) partition.setAllocated(true);
                            System.out.println("@t = " + t + " | partition " + (j + 1) + " | pages =  " + partition.getPages() + " | meToo = " + partition.getMetoo()[j]);
                            break;
                        }
                    }
                }
            }

            // builds string of free pages
            ArrayList<int[]> list = (getFreePageList(pageList));
            StringBuilder pagesList = new StringBuilder();
            for (int[] l : list) {
                pagesList.append(Arrays.toString(l));
            }

            fragmentationValues[t] = getFragmentation(pageList);
            System.out.println("@t = " + t + " |  fragmentation = " + getFragmentation(pageList) + " | pagesList = " + pagesList);
            System.out.println();
            choice = rn.nextInt(3);
        }

        // returns a csv of fragmentation values at each time step
        FileWriter writer = new FileWriter("fragmentation2.csv");
        for (double fragmentationValue : fragmentationValues) {
            writer.append(String.valueOf(fragmentationValue));
            writer.append("\n");
        }
        writer.close();

        System.out.println();
        System.out.println();

        ArrayList<MemoryPartition> partitionTest = new ArrayList<>();

        int[] pagesList = new int[20];

        partitionTest.add(new MemoryPartition(3, 1, 0, false));
        partitionTest.add(new MemoryPartition(2, 1, 1, false));
        partitionTest.get(1).setMetoo(new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1});
        partitionTest.add(new MemoryPartition(1, 1, 1, true));
        partitionTest.add(new MemoryPartition(10, 1, 0, false));
        partitionTest.add(new MemoryPartition(11, 1, 0, false));
        partitionTest.add(new MemoryPartition(1, 1, 0, false));
        partitionTest.add(new MemoryPartition(5, 1, 0, false));
        partitionTest.add(new MemoryPartition(5, 0, 1, false));
        partitionTest.add(new MemoryPartition(5, 0, 0, false));


        for (int i = 0; i < partitionTest.size(); i++) {
            MemoryPartition partition = partitionTest.get(i);


            if (!partition.isAllocated()) {
                if (roundNumber(rn.nextDouble()) < partition.getReq()) {
                    jIndex = 0;
                    boolean isIthFitAllocated = ithFit(partition, pagesList);
                    if (isIthFitAllocated) partition.setAllocated(true);
                    System.out.println("@t = " + i + " | REQUESTING partition " + (i + 1) + " | pages =  " + partition.getPages() + " | prob = " + partition.getReq());
                }
            }


            // Freeing simulation loop
            if (partition.isAllocated()) {
                if (roundNumber(rn.nextDouble()) < partition.getFree()) {
                    partition.setAllocated(false);
                    //System.out.println(partition.getPageIndexes());
                    for (int index : partition.getPageIndexes()) {
                        pagesList[index] = 0;
                    }
                    partition.getPageIndexes().clear();
                    //System.out.println(partition.getPageIndexes());
                    System.out.println("@t = " + i + " | FREEING partition " + (i + 1) + " | pages =  " + partition.getPages() + " | prob = " + partition.getFree());
                }
            }


            // MeToo simulation loop
            if (!partition.isAllocated()) {
                if (roundNumber(rn.nextDouble()) < getMeToo(i, jIndex, partitions)) {
                    boolean isIthFitAllocated = ithFit(partition, pagesList);
                    if (isIthFitAllocated) partition.setAllocated(true);
                    System.out.println("@t = " + i + " | partition " + (i + 1) + " | pages =  " + partition.getPages() + " | meToo = " + partition.getMetoo()[i]);
                }
            }

            ArrayList<int[]> page__list = getFreePageList(pagesList);
            StringBuilder pagesString = new StringBuilder();
            for (int[] l : page__list) {
                pagesString.append(Arrays.toString(l));
            }

            System.out.println("@t = " + i + " |  fragmentation = " + getFragmentation(pagesList) + " | pagesList = " + pagesString);
            System.out.println(Arrays.toString(pagesList));
            System.out.println();
        }
    }

    // will allocate a partition in every ith interval with i being the size of the partition
    public static boolean ithFit(MemoryPartition partition, int[] pageList) {
        int pagesRequested = partition.getPages();
        System.out.println("pages requested: " + pagesRequested);
        int start = 0, end = start + pagesRequested - 1;
        for (int j = 0; j < pageList.length; j++) {
            if (end >= pageList.length) {
                System.out.println("No page range available");
                return false;
            }
            int counter = 0, counter2 = 0;
            for (int s = start; s <= end; s++) {
                counter2++;
                if (pageList[s] == 0) counter++;
            }
            if (counter == pagesRequested)  {
                for (int k = start; k <= end; k++) {
                    partition.getPageIndexes().add(k);
                    pageList[k] = 1;
                }
                System.out.println("Page found at range " + start + " - " + end);
                // System.out.println("Index: " + j);
                return true;
            }
            start += counter2;
            end += counter2;
        }
        return true;
    }

    // returns the fragmentation percentage
    public static double getFragmentation(int[] pagesList) {
        int totalZeros = 0;
        for (int number : pagesList) {
            if (number == 0) totalZeros++;
        }

        int largestChunk = 0, chunk = 0;
        for (int j : pagesList) {
            if (j == 0) {
                chunk++;
            } else {
                if (chunk > largestChunk) {
                    largestChunk = chunk;
                    chunk = 0;
                }
            }
        }
        if (chunk > largestChunk) {
            largestChunk = chunk;
        }
        double fragmentation = (double) (totalZeros - largestChunk) / totalZeros;
        return roundNumber(fragmentation);
    }

    // returns an arraylist of integers containing the range of free pages
    public static ArrayList<int[]> getFreePageList(int[] pagesList) {
        int start = 0, end = 0;
        ArrayList<int[]> pages = new ArrayList<>();
        for (int i = 0; i < pagesList.length; i++) {
            if (pagesList[start] == 0 && pagesList[end] == 0) {
                end += 1;
            }
            else if (pagesList[start] == 0 && pagesList[end] == 1) {
                pages.add(new int[]{start, end - 1});
                end += 1;
                start = end;
            }
            else {
                start += 1;
                end += 1;
            }
        }
        if (start < end) {
            pages.add(new int[]{start, pagesList.length - 1});
        }
        return pages;
    }

    // returns the probability that i will be requested if j is allocated; returns 0 otherwise
    public static double getMeToo(int i, int j, ArrayList<MemoryPartition> partitions) {
        if (partitions.get(j).isAllocated() && !partitions.get(i).isAllocated()) {
            return partitions.get(i).getMetoo()[j];
        }
        return 0;
    }

    // generates random meToo array for each partition
    public static void generateMeToo(ArrayList<MemoryPartition> partitions) {
        Random rn = new Random();

        for (int i = 0; i < partitions.size(); i++) {
            double[] meToo = new double[partitions.size()];
            for (int j = 0; j < partitions.size(); j++) {
                double probability = roundNumber(rn.nextDouble());
                if (probability >= 0.8) {
                    meToo[j] = roundNumber(rn.nextDouble() / 1);
                }
                else if (probability >= 0.25 && probability <= 0.8) {
                    meToo[j] = roundNumber(rn.nextDouble() / 10);
                }
                else {
                    meToo[j] = roundNumber(rn.nextDouble() / 100);
                }
            }
            partitions.get(i).setMetoo(meToo);
        }
    }

    // generate sizes
    public static void generatePages(ArrayList<MemoryPartition> partitions) {
        Random rn = new Random();

        // generate probabilities
        for (MemoryPartition partition : partitions) {
            partition.setPages(rn.nextInt(10) + 1);
        }

        // overwrite with some smaller ones
        for (MemoryPartition partition : partitions) {
            if (roundNumber(rn.nextDouble()) <= 0.5) partition.setPages(rn.nextInt(3) + 1);
        }
        //  overwrite with some bigger ones
        for (MemoryPartition partition : partitions) {
            if (roundNumber(rn.nextDouble()) <= 0.1) partition.setPages(rn.nextInt(21));
        }
    }

    // generate free probabilities
    public static void generateFree(ArrayList<MemoryPartition> partitions) {
        Random rn = new Random();

        // generate probabilities
        for (MemoryPartition partition : partitions) {
            partition.setFree(roundNumber(rn.nextDouble() / 10));
        }

        // overwrite with some smaller ones
        for (MemoryPartition partition : partitions) {
            if (roundNumber(rn.nextDouble()) <= 0.5) partition.setFree(roundNumber(rn.nextDouble() / 100));
        }
        //  overwrite with some bigger ones
        for (MemoryPartition partition : partitions) {
            if (roundNumber(rn.nextDouble()) <= 0.5) partition.setFree(roundNumber(rn.nextDouble() / 1000));
        }
    }

    // generate req probabilities
    public static void generateReq(ArrayList<MemoryPartition> partitions) {
        Random rn = new Random();

        // generate probabilities
        for (MemoryPartition partition : partitions) {
            partition.setReq(roundNumber(rn.nextDouble() / 10));
        }

        // overwrite with some smaller ones
        for (MemoryPartition partition : partitions) {
            if (roundNumber(rn.nextDouble()) <= 0.5) partition.setReq(roundNumber(rn.nextDouble() / 100));
        }
        //  overwrite with some bigger ones
        for (MemoryPartition partition : partitions) {
            if (roundNumber(rn.nextDouble()) <= 0.5) partition.setReq(roundNumber(rn.nextDouble() / 1000));
        }
    }

    // rounds a number to two places
    public static double roundNumber(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}