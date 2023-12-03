package io.sim.reconciliation;

import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;

import io.sim.repport.ExcelRepport;

public class Rec extends Thread{

	double[] measureT;
	double[] stdevT;
	double[] measureD;
	double[] stdevD;

	public Rec(double[] _measureT, double[] _stdevT, double[] _measureD, double[] _stdevD)
	{
		super("Rec");
		this.measureT = _measureT;
		this.stdevT = _stdevT;
		this.measureD = _measureD;
		this.stdevD = _stdevD;
	}

	@Override
	public void run()
	{
		int nData = measureT.length;

		double[] At = new double[nData];
		double[] Ad = new double[nData];

		for(int i=0; i<nData;i++)
		{
			if (i == (nData -1))
			{
				At[i] = 1;
				Ad[i] = 1;
			}
			else
			{
				At[i] = -1;
				Ad[i] = -1;
			}
		}
		System.out.println(measureT.length);
		System.out.println(stdevT.length);
		System.out.println(At.length);
		Reconciliation recT = new Reconciliation(this.measureT, this.stdevT, At);
		System.out.println("Tempos reconciliados:");
		double[] reconciledT = recT.getReconciledFlow();
		recT.printMatrix(reconciledT);
		System.out.println(measureD);
		System.out.println(stdevD);
		System.out.println(Ad);
		Reconciliation recD = new Reconciliation(this.measureD, this.stdevD, Ad);
		System.out.println("Distancias reconciliados:");
		double[] reconciledD = recD.getReconciledFlow();
		recT.printMatrix(reconciledD);

		try {
			ExcelRepport.setReconciliation(reconciledT, reconciledD);
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
	}

	// public static void main(String[] args) {

	// 	// F1 F3 F5 F6
	// 	// =====>O=====>O=====>O=====>
	// 	// | ^
	// 	// | F2 F4 |
	// 	// ======>O======

	// 	double[] y = new double[] { 110.5, 60.8, 35.0, 68.9, 38.6, 101.4 };

	// 	double[] v = new double[] { 0.6724, 0.2809, 0.2116, 0.5041, 0.2025, 1.44 };

	// 	double[][] A = new double[][] { { 1, -1, -1, 0, 0, 0 }, { 0, 1, 0, -1, 0, 0 }, { 0, 0, 1, 0, -1, 0 },
	// 			{ 0, 0, 0, 1, 1, -1 } };

	// 	Reconciliation rec = new Reconciliation(y, v, A);
	// 	System.out.println("Y_hat:");
	// 	rec.printMatrix(rec.getReconciledFlow());
	// }

}
