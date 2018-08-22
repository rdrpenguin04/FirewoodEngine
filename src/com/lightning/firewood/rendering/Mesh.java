package com.lightning.firewood.rendering;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.lightning.firewood.loading.ResourceType;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Mesh extends ResourceType {
	private int vbo;
	private int ibo;
	private int size;
	
	public Mesh() {
		vbo = glGenBuffers();
		ibo = glGenBuffers();
	}
	
	protected void finalize() {
		glDeleteBuffers(vbo);
		glDeleteBuffers(ibo);
	}
	
	public Mesh(File f) {
		this();
		load(f);
	}
	
	public void load(File f) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			String extension = "";

			int extIndex = f.getName().lastIndexOf('.');

			if(extIndex > 0) {
			    extension = f.getName().substring(extIndex+1);
			}
			
			if(extension.equals("obj")) {
				ArrayList<Float> x = new ArrayList<>();
				ArrayList<Float> y = new ArrayList<>();
				ArrayList<Float> z = new ArrayList<>();
				ArrayList<Float> nx = new ArrayList<>();
				ArrayList<Float> ny = new ArrayList<>();
				ArrayList<Float> nz = new ArrayList<>();
				ArrayList<Float> u = new ArrayList<>();
				ArrayList<Float> v = new ArrayList<>();
				ArrayList<Integer> pos = new ArrayList<>();
				ArrayList<Integer> tex = new ArrayList<>();
				ArrayList<Integer> norm = new ArrayList<>();
				
				Scanner s = new Scanner(f);
				while(s.hasNextLine()) {
					String[] line = s.nextLine().split(" ");
					
					if(line[0] == "v") {
						x.add(Float.parseFloat(line[1]));
						y.add(Float.parseFloat(line[2]));
						z.add(Float.parseFloat(line[3]));
					} else if(line[0] == "vt") {
						u.add(Float.parseFloat(line[1]));
						v.add(Float.parseFloat(line[2]));
					} else if(line[0] == "vn") {
						nx.add(Float.parseFloat(line[1]));
						ny.add(Float.parseFloat(line[2]));
						nz.add(Float.parseFloat(line[3]));
					} else if(line[0] == "f") {
						String[] firstFace = line[1].split("/");
						String[] prevFace = line[2].split("/");
						for(int i = 3; i < line.length; i++) {
							String[] curFace = line[3].split("/");
							pos.add(Integer.parseInt(firstFace[0]));
							pos.add(Integer.parseInt(prevFace[0]));
							pos.add(Integer.parseInt(curFace[0]));
							if(firstFace.length == 2 || (firstFace.length > 2 && !firstFace[1].equals(""))) {
								tex.add(Integer.parseInt(firstFace[1]));
								tex.add(Integer.parseInt(prevFace[1]));
								tex.add(Integer.parseInt(curFace[1]));
							}
							if(firstFace.length == 3) {
								norm.add(Integer.parseInt(firstFace[2]));
								norm.add(Integer.parseInt(prevFace[2]));
								norm.add(Integer.parseInt(curFace[2]));
							}
							prevFace = curFace;
						}
					}
				}
				
				ArrayList<Integer> finalInd = new ArrayList<>();
				ArrayList<ArrayList<Integer>> existingInd = new ArrayList<>();
				for(int i = 0; i < pos.size(); i++) {
					ArrayList<Integer> curInd = new ArrayList<>();
					curInd.add(pos.get(i));
					if(!tex.isEmpty()) curInd.add(tex.get(i));
					if(!norm.isEmpty()) curInd.add(norm.get(i));
					int index = existingInd.indexOf(curInd);
					if(index == -1) {
						existingInd.add(curInd);
						index = existingInd.size()-1;
					}
					finalInd.add(index);
				}
				
				int[] finalIndArray = finalInd.stream().mapToInt(i->i).toArray();
				
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, finalIndArray, GL_STATIC_DRAW);
				
				ArrayList<Float> finalVert = new ArrayList<>();
				int normInd = tex.isEmpty() ? 1 : 2;
				
				for(ArrayList<Integer> ind : existingInd) {
					finalVert.add(x.get(ind.get(0)));
					finalVert.add(y.get(ind.get(0)));
					finalVert.add(z.get(ind.get(0)));
					if(!tex.isEmpty()) {
						finalVert.add(u.get(ind.get(1)));
						finalVert.add(v.get(ind.get(1)));
					} else {
						finalVert.add(0f);
						finalVert.add(0f);
					}
					if(!norm.isEmpty()) {
						finalVert.add(nx.get(ind.get(normInd)));
						finalVert.add(ny.get(ind.get(normInd)));
						finalVert.add(nz.get(ind.get(normInd)));
					} else {
						finalVert.add(0f);
						finalVert.add(0f);
						finalVert.add(1f);
					}
				}
				
				float[] finalVertArray = new float[finalVert.size()];
				
				for(int i = 0; i < finalVertArray.length; i++) {
					finalVertArray[i] = finalVert.get(i);
				}
				
				glBindBuffer(GL_ARRAY_BUFFER, vbo);
				glBufferData(GL_ARRAY_BUFFER, finalVertArray, GL_STATIC_DRAW);
				
				size = finalIndArray.length;
				
				s.close();
			} else {
				throw new IllegalArgumentException("Error: Cannot parse ." + extension + " files!");
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			if(fis != null) {
				try { fis.close(); } catch (IOException e) { System.out.println("sadf");}
			}
		}
	}
	
	public void render() {
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 32, 12);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 32, 20);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glDrawElements(GL_TRIANGLES, size, GL_UNSIGNED_INT, 0);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
	}
}
