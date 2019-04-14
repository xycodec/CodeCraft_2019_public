package com.huawei.graph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import com.huawei.utils.Constant;
import com.huawei.utils.Cross_info;
import com.huawei.utils.Direction;
import com.huawei.utils.Path;
import com.huawei.utils.link_pair;
import com.huawei.utils.node_pair;
import com.huawei.vehicle.car;
import com.huawei.vehicle.car_comparator;

public class graph {
	public static double INF=100000000;
	public ArrayList<cross_node> head=new ArrayList<>();//节点集合
	Map<node_pair,road_link> node_pair_to_link=new HashMap<>();//节点对映射到道路(两个node决定一个link)
	Map<link_pair,Integer> link_pair_to_node=new HashMap<>();//映射连接link_1,link_2的cross_id
	Map<node_pair,Double> node_pair_to_link_tmp=new HashMap<>();//节点对映射到cost
	Map<link_pair,Direction> dir_mp=new HashMap<>();//方向映射,不写调度器的话暂时用不到
	public graph() {

	}
	
	Map<Integer,Integer> real_id_mp=new HashMap<>();//cross索引映射到真实的id(真实的id未必连续,但cross索引必须是连续的)
	Map<Integer,Integer> r_real_id_mp=new HashMap<>();//与real_id_mp的映射相反
	
	Map<Integer,Integer> id_to_degree_in=new HashMap<>();//node_id到入度数的映射
	Map<Integer,Integer> id_to_degree_out=new HashMap<>();//node_id到出度数的映射
	Map<Integer,Integer> id_to_double=new HashMap<>();//road_id到是否是双向边(=1表示是双向边)的映射
	Map<Integer,node_pair> id_to_node_pair=new HashMap<>();//记录单向边road_id与对应的节点对的映射
	public void init3(String carPath,String roadPath,
			String crossPath,String preAnsPath) {
		Cross_info.read(carPath,roadPath,crossPath,preAnsPath);
		
		ArrayList<ArrayList<Integer>> road_info=Cross_info.Roads;
		ArrayList<ArrayList<Integer>> cross_info=Cross_info.Crosses;
		for(int i=0;i<cross_info.size()+1;++i) {//添加节点
			head.add(new cross_node(i));
		}
		
		for(int i=0;i<cross_info.size();++i) {//添加节点,0位置不用,1~...,并设置映射
			int id=cross_info.get(i).get(0);
			real_id_mp.put(id, i+1);//真实的id(可能是不连续的数字或其他类型),映射到i+1(0位置不用,1~...)
			r_real_id_mp.put(i+1, id);
			head.get(i+1).cross_id=id;
		}
		
		//设置节点之间连接的信息与边的信息,相对位置的信息还不知道,需从cross_info中得到
		for(int i=0;i<road_info.size();++i) {
			int road_id=road_info.get(i).get(0);
			int length=road_info.get(i).get(1);
			int speed_limit=road_info.get(i).get(2);
			int lane_num=road_info.get(i).get(3);
			int src_id=road_info.get(i).get(4);
			int dst_id=road_info.get(i).get(5);
			int is_double=road_info.get(i).get(6);
			id_to_double.put(road_id,is_double);
			if(is_double==1) {//双向边src_id->dst_id,dst_id->src_id
				road_link tmp1=new road_link(road_id, length, speed_limit, lane_num, src_id, dst_id);
				road_link tmp2=new road_link(road_id, length, speed_limit, lane_num, dst_id, src_id);
				//src->dst
				//real_id_mp.get(src_id),src_id本身可能不连续或者是其他乱七八糟的数据,把它映射到连续的数字上,数字从1开始....,下同
				cross_node tmp_node=new cross_node(dst_id);//这里不用映射,dst_id是cross_node的本身属性,下同
				if(head.get(real_id_mp.get(src_id)).next_node==null) {
					head.get(real_id_mp.get(src_id)).next_node=tmp_node;
				}else {
					tmp_node.next_node=head.get(real_id_mp.get(src_id)).next_node;
					head.get(real_id_mp.get(src_id)).next_node=tmp_node;
				}
				//dst->src
				tmp_node=new cross_node(src_id);
				if(head.get(real_id_mp.get(dst_id)).next_node==null) {
					head.get(real_id_mp.get(dst_id)).next_node=tmp_node;
				}else {
					tmp_node.next_node=head.get(real_id_mp.get(dst_id)).next_node;
					head.get(real_id_mp.get(dst_id)).next_node=tmp_node;
				}
				node_pair tmp_node_sd=new node_pair(src_id, dst_id);
				node_pair tmp_node_ds=new node_pair(dst_id, src_id);
				node_pair_to_link.put(tmp_node_sd, tmp1);
				node_pair_to_link.put(tmp_node_ds, tmp2);
				node_pair_to_link_tmp.put(tmp_node_sd, tmp1.cost);
				node_pair_to_link_tmp.put(tmp_node_ds, tmp2.cost);
			}else {//src->dst
				
				id_to_node_pair.put(road_id, new node_pair(src_id, dst_id));
				
				road_link tmp1=new road_link(road_id, length, speed_limit, lane_num, src_id, dst_id);
				//src->dst
				cross_node tmp_node=new cross_node(dst_id);
				if(head.get(real_id_mp.get(src_id)).next_node==null) {
					head.get(real_id_mp.get(src_id)).next_node=tmp_node;
				}else {
					tmp_node.next_node=head.get(real_id_mp.get(src_id)).next_node;
					head.get(real_id_mp.get(src_id)).next_node=tmp_node;
				}
				node_pair tmp_node_sd=new node_pair(src_id, dst_id);
				node_pair_to_link.put(tmp_node_sd, tmp1);
				node_pair_to_link_tmp.put(tmp_node_sd, tmp1.cost);
			}
		}
	
		//存储方向信息,顺便映射link_pair到cross_node,两个link夹一个node(为了解析preset_cars的path).两个node决定一个link
		//统计节点的度数信息
		for(int i=0;i<cross_info.size();++i) {
			int cross_id=cross_info.get(i).get(0);
			int up_id=cross_info.get(i).get(1);
			int right_id=cross_info.get(i).get(2);
			int down_id=cross_info.get(i).get(3);
			int left_id=cross_info.get(i).get(4);
			if(up_id!=-1) {
				if(id_to_double.containsKey(up_id)) {//是双向边
					//入度
					if(!id_to_degree_in.containsKey(cross_id))
						id_to_degree_in.put(cross_id, 1);
					else
						id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					//出度
					if(!id_to_degree_out.containsKey(cross_id))
						id_to_degree_out.put(cross_id, 1);
					else
						id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
				}else {//单向边
					node_pair tmp_node=id_to_node_pair.get(up_id);//根据单向边的road_id得到node_pair
					if(tmp_node.src_id==cross_id) {//出边
						//出度
						if(!id_to_degree_out.containsKey(cross_id))
							id_to_degree_out.put(cross_id, 1);
						else
							id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
					}else {//入边（tmp_node.dst_id==cross_id）
						//入度
						if(!id_to_degree_in.containsKey(cross_id))
							id_to_degree_in.put(cross_id, 1);
						else
							id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					}	
				}
				
				if(right_id!=-1) {
					dir_mp.put(new link_pair(up_id, right_id), Direction.LEFT);
					link_pair_to_node.put(new link_pair(up_id, right_id), cross_id);
				}
				if(down_id!=-1) {
					dir_mp.put(new link_pair(up_id, down_id), Direction.DIRECT);
					link_pair_to_node.put(new link_pair(up_id, down_id), cross_id);
				}
				if(left_id!=-1) {
					dir_mp.put(new link_pair(up_id, left_id), Direction.RIGHT);
					link_pair_to_node.put(new link_pair(up_id, left_id), cross_id);
				}
			}
			if(right_id!=-1) {
				if(id_to_double.containsKey(right_id)) {//是双向边
					//入度
					if(!id_to_degree_in.containsKey(cross_id))
						id_to_degree_in.put(cross_id, 1);
					else
						id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					//出度
					if(!id_to_degree_out.containsKey(cross_id))
						id_to_degree_out.put(cross_id, 1);
					else
						id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
				}else {//单向边
					node_pair tmp_node=id_to_node_pair.get(right_id);//根据单向边的road_id得到node_pair
					if(tmp_node.src_id==cross_id) {//出边
						//出度
						if(!id_to_degree_out.containsKey(cross_id))
							id_to_degree_out.put(cross_id, 1);
						else
							id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
					}else {//入边（tmp_node.dst_id==cross_id）
						//入度
						if(!id_to_degree_in.containsKey(cross_id))
							id_to_degree_in.put(cross_id, 1);
						else
							id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					}	
				}
				if(up_id!=-1) {
					dir_mp.put(new link_pair(right_id, up_id), Direction.RIGHT);
					link_pair_to_node.put(new link_pair(right_id, up_id), cross_id);
				}
				if(down_id!=-1) {
					dir_mp.put(new link_pair(right_id, down_id), Direction.LEFT);
					link_pair_to_node.put(new link_pair(right_id, down_id), cross_id);
				}
				if(left_id!=-1) {
					dir_mp.put(new link_pair(right_id, left_id), Direction.DIRECT);
					link_pair_to_node.put(new link_pair(right_id, left_id), cross_id);
				}
			}
			if(down_id!=-1) {
				if(id_to_double.containsKey(down_id)) {//是双向边
					//入度
					if(!id_to_degree_in.containsKey(cross_id))
						id_to_degree_in.put(cross_id, 1);
					else
						id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					//出度
					if(!id_to_degree_out.containsKey(cross_id))
						id_to_degree_out.put(cross_id, 1);
					else
						id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
				}else {//单向边
					node_pair tmp_node=id_to_node_pair.get(down_id);//根据单向边的road_id得到node_pair
					if(tmp_node.src_id==cross_id) {//出边
						//出度
						if(!id_to_degree_out.containsKey(cross_id))
							id_to_degree_out.put(cross_id, 1);
						else
							id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
					}else {//入边（tmp_node.dst_id==cross_id）
						//入度
						if(!id_to_degree_in.containsKey(cross_id))
							id_to_degree_in.put(cross_id, 1);
						else
							id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					}	
				}
				if(up_id!=-1) {
					dir_mp.put(new link_pair(down_id, up_id), Direction.DIRECT);
					link_pair_to_node.put(new link_pair(down_id, up_id), cross_id);
				}
				if(right_id!=-1) {
					dir_mp.put(new link_pair(down_id, right_id), Direction.RIGHT);
					link_pair_to_node.put(new link_pair(down_id, right_id), cross_id);
				}
				if(left_id!=-1) {
					dir_mp.put(new link_pair(down_id, left_id), Direction.LEFT);
					link_pair_to_node.put(new link_pair(down_id, left_id), cross_id);
				}
			}
			if(left_id!=-1) {
				if(id_to_double.containsKey(left_id)) {//是双向边
					//入度
					if(!id_to_degree_in.containsKey(cross_id))
						id_to_degree_in.put(cross_id, 1);
					else
						id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					//出度
					if(!id_to_degree_out.containsKey(cross_id))
						id_to_degree_out.put(cross_id, 1);
					else
						id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
				}else {//单向边
					node_pair tmp_node=id_to_node_pair.get(left_id);//根据单向边的road_id得到node_pair
					if(tmp_node.src_id==cross_id) {//出边
						//出度
						if(!id_to_degree_out.containsKey(cross_id))
							id_to_degree_out.put(cross_id, 1);
						else
							id_to_degree_out.put(cross_id, 1+id_to_degree_out.get(cross_id));
					}else {//入边（tmp_node.dst_id==cross_id）
						//入度
						if(!id_to_degree_in.containsKey(cross_id))
							id_to_degree_in.put(cross_id, 1);
						else
							id_to_degree_in.put(cross_id, 1+id_to_degree_in.get(cross_id));
					}	
				}
				if(up_id!=-1) {
					dir_mp.put(new link_pair(left_id, up_id), Direction.LEFT);
					link_pair_to_node.put(new link_pair(left_id, up_id), cross_id);
				}
				if(right_id!=-1) {
					dir_mp.put(new link_pair(left_id, right_id), Direction.DIRECT);
					link_pair_to_node.put(new link_pair(left_id, right_id), cross_id);
				}
				if(down_id!=-1) {
					dir_mp.put(new link_pair(left_id, down_id), Direction.RIGHT);
					link_pair_to_node.put(new link_pair(left_id, down_id), cross_id);
				}
			}
		}
		for(int i=1;i<head.size();++i) {
//			System.out.printf("cross_%d\tin:%d,out:%d\n",head.get(i).cross_id,
//					id_to_degree_in.get(head.get(i).cross_id),id_to_degree_out.get(head.get(i).cross_id));
		}
		System.out.println();
		//根据入度与出度计算每条链路的degree
		double max_degree=-1;
		for(node_pair tmp:node_pair_to_link.keySet()) {
			if(max_degree<(id_to_degree_in.get(tmp.src_id)+id_to_degree_in.get(tmp.dst_id))*(id_to_degree_out.get(tmp.src_id)+id_to_degree_out.get(tmp.dst_id)))
				max_degree=(id_to_degree_in.get(tmp.src_id)+id_to_degree_in.get(tmp.dst_id))*(id_to_degree_out.get(tmp.src_id)+id_to_degree_out.get(tmp.dst_id));
			node_pair_to_link.get(tmp).degree=(id_to_degree_in.get(tmp.src_id)+id_to_degree_in.get(tmp.dst_id))*(id_to_degree_out.get(tmp.src_id)+id_to_degree_out.get(tmp.dst_id));
			
		}
		//degree越大,node越稳健
		for(road_link tmp:node_pair_to_link.values()) {
//			System.out.printf("road_%d\tdegree:%f\n",tmp.road_id,tmp.degree);
			tmp.cost*=Math.pow(tmp.degree/max_degree,1.0/3);
		}
		s=new boolean[head.size()+1];
		prev_node=new int[head.size()+1];
		dist=new double[head.size()+1];
	}
	
	//路径信息存储在path数组中(在前向节点集合中前向查找)
	public ArrayList<Integer> getPath(int s,int t){
		//s,t应该是映射后的连续的数字
		s=real_id_mp.get(s);
		t=real_id_mp.get(t);
		ArrayList<Integer> v=new ArrayList<>();
		if(s==t) return v;
		v.add(r_real_id_mp.get(t));
		int tmp=t;
		while(tmp!=-1){
			tmp=prev_node[tmp];//tmp的前向节点
			if(tmp!=-1) v.add(r_real_id_mp.get(tmp));
		}
		Collections.reverse(v);
		return v;
	}
	
	public boolean[] s;//待扩展的节点集合,false:未扩展
	public int[] prev_node;//存放前向节点
	public double[] dist;//路径的权重和
	public Path Dijkstra(int v0,int t){//v0-> otherNode
		//这里的参数应该是映射后的(连续的数字)
		v0=real_id_mp.get(v0);
		t=real_id_mp.get(t);
		for(int i=1;i<head.size();++i) {
			dist[i]=INF;
			s[i]=false;
			prev_node[i]=-1;
		}
		cross_node tmp_node=head.get(v0).next_node;
		while(tmp_node!=null) {//初始化
			dist[real_id_mp.get(tmp_node.cross_id)]=node_pair_to_link.get(new node_pair(r_real_id_mp.get(v0), tmp_node.cross_id)).cost;
			prev_node[real_id_mp.get(tmp_node.cross_id)]=v0;
			tmp_node=tmp_node.next_node;
		}
		prev_node[v0]=-1;
		s[v0]=true;
		dist[v0]=0;
		
		for(int i=1;i<head.size()-1;++i) {
			double min=INF;
			int u=v0;
			for(int j=1;j<head.size();++j) {//选择当前集合T中具有最短路径的顶点 u
				if(!s[j]&&dist[j]<min) {
					u=j;
					min=dist[j];
				}
			}
			s[u]=true;//将顶点u加入到集合s，表示它的最短路径已求得
			tmp_node=head.get(u).next_node;
			while(tmp_node!=null) {
				road_link tmp_link=node_pair_to_link.get(new node_pair(r_real_id_mp.get(u), tmp_node.cross_id));
				if(!s[real_id_mp.get(tmp_node.cross_id)]&&dist[u]+tmp_link.cost<dist[real_id_mp.get(tmp_node.cross_id)]) {
					dist[real_id_mp.get(tmp_node.cross_id)]=dist[u]+tmp_link.cost;
					prev_node[real_id_mp.get(tmp_node.cross_id)]=u;
				}
				tmp_node=tmp_node.next_node;
			}
		}
		return new Path(getPath(r_real_id_mp.get(v0), r_real_id_mp.get(t)), dist[t]);
	}
	
	public Path Dijkstra_tmp(int v0,int t){//v0-> otherNode
		//这里的参数应该是映射后的(连续的数字)
		v0=real_id_mp.get(v0);
		t=real_id_mp.get(t);
//		System.out.println(v0+"->"+t);
		for(int i=1;i<head.size();++i) {
			dist[i]=INF;
			prev_node[i]=-1;
		}
		cross_node tmp_node=head.get(v0).next_node;
		while(tmp_node!=null) {//初始化
			dist[real_id_mp.get(tmp_node.cross_id)]=node_pair_to_link.get(new node_pair(r_real_id_mp.get(v0), tmp_node.cross_id)).cost;
			prev_node[real_id_mp.get(tmp_node.cross_id)]=v0;
			tmp_node=tmp_node.next_node;
		}
		prev_node[v0]=-1;
		s[v0]=true;
		dist[v0]=0;
		
		for(int i=1;i<head.size()-1;++i) {
			double min=INF;
			int u=v0;
			for(int j=1;j<head.size();++j) {//选择当前集合T中具有最短路径的顶点 u
				if(!s[j]&&dist[j]<min) {
					u=j;
					min=dist[j];
				}
			}
			s[u]=true;//将顶点u加入到集合s，表示它的最短路径已求得
			tmp_node=head.get(u).next_node;
			while(tmp_node!=null) {
				road_link tmp_link=node_pair_to_link.get(new node_pair(r_real_id_mp.get(u), tmp_node.cross_id));
				if(!s[real_id_mp.get(tmp_node.cross_id)]&&dist[u]+tmp_link.cost<dist[real_id_mp.get(tmp_node.cross_id)]) {
					dist[real_id_mp.get(tmp_node.cross_id)]=dist[u]+tmp_link.cost;
					prev_node[real_id_mp.get(tmp_node.cross_id)]=u;
				}
				tmp_node=tmp_node.next_node;
			}
		}
		return new Path(getPath(r_real_id_mp.get(v0), r_real_id_mp.get(t)), dist[t]);
	}
	
	
	public ArrayList<Path> ans_path=new ArrayList<>();//YEN_ksp计算出的路径集合
	public Map<Path, Boolean> dev_path_sql=new HashMap<Path, Boolean>();//用于查询偏离路径是否重复
	public Queue<Path> dev_path_queue = new PriorityQueue<>();//用于存放偏离路径
	public void set_link(ArrayList<Integer> p1,int s_node) {//设置偏离路径为INF,p1:待检测的path,s_node:p1的末结点
		int len=ans_path.size();
		int len_p=p1.size();
		for(int j=0;j<len_p;++j) {
			s[real_id_mp.get(p1.get(j))]=true;
		}
		for(int i=0;i<len;++i) {
			ArrayList<Integer> p2=ans_path.get(i).path;
			if(len_p>=p2.size()) continue;
			boolean flag=true;
			for(int j=0;j<len_p;++j) {
				if(p1.get(j)!=p2.get(j)) {
					flag=false;
					break;
				}
			}
			if(flag==false) continue;
			//前面的结点都一样,然后就看扩展结点的了
			cross_node tmp_node=head.get(real_id_mp.get(s_node)).next_node;
			while(tmp_node!=null) {//从s_node扩展结点
				if(tmp_node.cross_id==p2.get(len_p)) {
					node_pair_to_link_tmp.put(new node_pair(s_node,p2.get(len_p)), INF);
				}
				tmp_node=tmp_node.next_node;
			}
		}
		
	}
	
	public void recover_link(int s_node) {
		cross_node tmp_node=head.get(real_id_mp.get(s_node)).next_node;
		while(tmp_node!=null) {
			node_pair_to_link_tmp.put(new node_pair(s_node, tmp_node.cross_id), node_pair_to_link.get(new node_pair(s_node, tmp_node.cross_id)).cost);
			tmp_node=tmp_node.next_node;
		}
		for(int i=1;i<head.size();++i) {
			s[i]=false;
		}
	}
	
	public void clear() {
		ans_path.clear();
		dev_path_queue.clear();
		dev_path_sql.clear();
		ib_mp.clear();
		for(int i=1;i<head.size();++i) {
			dist[i]=INF;
			prev_node[i]=-1;
			s[i]=false;
		}
	}
	
	public void Yen_ksp(int s,int t,int k) {
		//这里的参数应该是映射后的(连续的数字)
		if(s==t||k<=0) return;
		//copy
		for(int i=1;i<head.size();++i){
			cross_node tmp_node=head.get(i).next_node;
			while(tmp_node!=null) {
				node_pair_to_link_tmp.put(new node_pair(r_real_id_mp.get(i), tmp_node.cross_id), node_pair_to_link.get(new node_pair(r_real_id_mp.get(i), tmp_node.cross_id)).cost);
				tmp_node=tmp_node.next_node;
			}
		}
		
		Path p=Dijkstra_tmp(s,t);//最短路,即最初的迭代路径
		if(k==1) {
			ans_path.add(p);
			return;
		}
		while(k-->0) {
			if(!ans_path.contains(p)) ans_path.add(p);
			else continue;
			int len=p.path.size();
			ArrayList<Integer> path_tmp=new ArrayList<>();//p的部分迭代路径
			double dist_tmp=0;
			for(int i=0;i<len-1;++i) {
				if(i>=1) {
					dist_tmp+=node_pair_to_link_tmp.get(new node_pair(p.path.get(i-1),p.path.get(i)));
				}
				path_tmp.add(p.path.get(i));
				set_link(path_tmp,p.path.get(i));//设置偏离路径(边权值设为INF)
				
				Dijkstra_tmp(p.path.get(i),t);
				
				recover_link(p.path.get(i));
				if(dist[real_id_mp.get(t)]>=INF) continue;//没有路径了
				ArrayList<Integer> path_tmp2=(ArrayList<Integer>)path_tmp.clone();
				path_tmp2.remove(path_tmp2.size()-1);
				path_tmp2.addAll(getPath(p.path.get(i), t));
				
				Path pp=new Path(path_tmp2,dist_tmp+dist[real_id_mp.get(t)]);//修正后的最短路(偏离路径)
				if(!dev_path_queue.contains(pp)) {
					dev_path_queue.add(pp);
				}
			}
			if(dev_path_queue.isEmpty()) break;
			p=dev_path_queue.remove();
		}
	}
	
	public Map<node_pair,Integer> ni_mp=new HashMap<>();//想办法统计出(s,t)的频率,即从起点s到终点t的车子数目,尽量让它们分散开
	public Map<node_pair,ArrayList<car>> nc_mp=new HashMap<>();//节点对(起点到终点)映射到车子(起点到终点是该节点对)的集合
	
	public Map<Integer,Boolean> ib_mp=new HashMap<>();//记录本轮指定id的road_link有没有使用到
	public Map<Integer,ArrayList<Integer>> id_to_path=new HashMap<>();//car_id->路径序列
	public Map<Integer,Integer> preset_time_to_cnt=new HashMap<>();//预设车辆对于时间点的占用次数
	public Map<Integer,car> id_to_car=new HashMap<>();//car_id映射到对应的car
	public static void calc4(String carPath,String roadPath,
			String crossPath,String preAnsPath,String answerPath) {
		graph g1=new graph();
		g1.init3(carPath,roadPath,crossPath,preAnsPath);
		ArrayList<ArrayList<Integer>> pre_ans=Cross_info.PreAns;//预置车辆
		PrintWriter pout=null;
		try {
			pout=new PrintWriter(new FileWriter(answerPath),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Random r=new Random();
		ArrayList<ArrayList<Integer>> car_info=Cross_info.Cars;//车子信息
		ArrayList<car> cars=new ArrayList<>();//car_info只是一些ArrayList,需要将信息装填到car对象中
		Map<Integer,Integer> preset_to_priority=new HashMap<>();//preset的并且是priority的,id到这种属性的映射
		for(int i=0;i<car_info.size();++i) {
			car tmp_car=new car();
			tmp_car.car_id=car_info.get(i).get(0);
			tmp_car.from=car_info.get(i).get(1);
			tmp_car.to=car_info.get(i).get(2);
			tmp_car.max_speed=car_info.get(i).get(3);
			tmp_car.start_time=car_info.get(i).get(4);
			tmp_car.priority=car_info.get(i).get(5);
			tmp_car.preset=car_info.get(i).get(6);
			if(tmp_car.preset==1&&tmp_car.priority==1) {
				preset_to_priority.put(tmp_car.car_id, 1);
			}
			//构建节点对(起点到终点)到车子(起点到终点是该节点对)的集合的映射
			node_pair tmp_node=new node_pair(tmp_car.from, tmp_car.to);
			if(g1.nc_mp.containsKey(tmp_node))
				g1.nc_mp.get(tmp_node).add(tmp_car);
			else {
				g1.nc_mp.put(tmp_node,new ArrayList<>());
				g1.nc_mp.get(tmp_node).add(tmp_car);
			}
			//统计出(s,t)的频率,即从起点s到终点t的车子数目
			if(g1.ni_mp.containsKey(tmp_node))
				g1.ni_mp.put(tmp_node, 1+g1.ni_mp.get(new node_pair(tmp_car.from, tmp_car.to)));
			else
				g1.ni_mp.put(tmp_node, 1);
			
			cars.add(tmp_car);
			g1.id_to_car.put(tmp_car.car_id, tmp_car);
		}
		int cnt=0;
		//输出一下统计数据
		//起点与终点数据,看起来分布还是不太均匀的,不如就按照统计得到的顺序来好了
		for(node_pair tmp:g1.ni_mp.keySet()) {
//			System.out.printf("(%d,%d) : %d\n",tmp.src_id,tmp.dst_id,g1.ni_mp.get(tmp));
			if(g1.ni_mp.get(tmp)>50) {
				for(car c:g1.nc_mp.get(new node_pair(tmp.src_id, tmp.dst_id))) {
					c.is_hard=true;
					++cnt;
				}
			}
		}
		
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		Collections.sort(cars);//speed从大到小,start_time,car_id从小到大,实际上这种机制很低效???
//		for(int i=0;i<5;++i) Collections.shuffle(cars);
		int preset_min_time=1000000,preset_max_time=-1;//预置的优先车辆的最早启动时间与最迟启动时间
		int preset_priority_min_time=10000000,preset_priority_max_time=-1;
		//记录预置车辆的id与路径序列之间的映射(路径序列前面还有car_id和start_time)
		for(int i=0;i<pre_ans.size();++i) {
			g1.id_to_car.get(pre_ans.get(i).get(0)).real_time=pre_ans.get(i).get(1);
			if(preset_min_time>pre_ans.get(i).get(1))
				preset_min_time=pre_ans.get(i).get(1);
			if(preset_max_time<pre_ans.get(i).get(1))
				preset_max_time=pre_ans.get(i).get(1);
			
			if(preset_to_priority.containsKey(pre_ans.get(i).get(0))) {//preset的且priority的
				if(preset_priority_min_time>pre_ans.get(i).get(1))
					preset_priority_min_time=pre_ans.get(i).get(1);
				if(preset_priority_max_time<pre_ans.get(i).get(1))
					preset_priority_max_time=pre_ans.get(i).get(1);
			}
			g1.id_to_path.put(pre_ans.get(i).get(0), pre_ans.get(i));
			//记录预设车辆对于时间点的占用次数
			if(!g1.preset_time_to_cnt.containsKey(pre_ans.get(i).get(1)))
				g1.preset_time_to_cnt.put(pre_ans.get(i).get(1),1);
			else
				g1.preset_time_to_cnt.put(pre_ans.get(i).get(1),1+g1.preset_time_to_cnt.get(pre_ans.get(i).get(1)));
		}
		
		ArrayList<car> preset_cars=new ArrayList<>();
		ArrayList<car> preset_priority_cars=new ArrayList<>();
		ArrayList<car> priority_cars=new ArrayList<>();//不算预置的车辆
		ArrayList<car> normal_cars=new ArrayList<>();//普通优先级的车辆
		for(int i=0;i<cars.size();++i) {
			if(cars.get(i).preset==1) {
				preset_cars.add(cars.get(i));
				if(cars.get(i).priority==1) {
					preset_priority_cars.add(cars.get(i));
				}
			}else {
				if(cars.get(i).priority==1) {
					priority_cars.add(cars.get(i));
				}else {
					normal_cars.add(cars.get(i));
				}
			}
		}
		int preset_limit=preset_cars.size()/10;
		Collections.sort(preset_cars, new car_comparator());
		
		System.out.println(preset_cars.get(preset_cars.size()-1-preset_limit).real_time);
		System.out.println(preset_min_time+" "+preset_max_time);
		System.out.println(preset_priority_min_time+" "+preset_priority_max_time);
		System.out.println("preset: "+preset_cars.size());
		System.out.println("preset_priority: "+preset_priority_cars.size());
		System.out.println("priority: "+priority_cars.size());
		System.out.println("normal: "+normal_cars.size());
		System.out.println((preset_cars.size()+priority_cars.size()+normal_cars.size())==cars.size());
		//预置车辆无需规划路径(更新一下road_link的cost)
		int time_line=0;
		int s_time=preset_cars.get(preset_cars.size()-1-preset_limit).real_time;
		for(int i=0;i<preset_cars.size()-preset_limit+2;++i) {
//			if(preset_cars.get(i).priority==1)
//				System.out.println(preset_cars.get(i).real_time);
			//设置preset_car影响的链路的权值
			ArrayList<Integer> tmp=new ArrayList<>();
			int car_from=preset_cars.get(i).from,car_to=preset_cars.get(i).to;
			//j从2开始,因为前面两个是car_id和start_time
			//注意路径序列是由link_id组成的,tmp存放link_id序列
			for(int j=2;j<g1.id_to_path.get(preset_cars.get(i).car_id).size();++j) {
				tmp.add(g1.id_to_path.get(preset_cars.get(i).car_id).get(j));
			}
			ArrayList<Integer> tmp2=new ArrayList<>();
			tmp2.add(car_from);
			for(int j=0;j<tmp.size()-1;++j) {//此时tmp里面存放着link_id序列,要从link_pair得到node
//				System.out.println(tmp.get(j)+" "+tmp.get(j+1)+":"+g1.link_pair_to_node.get(new link_pair(tmp.get(j), tmp.get(j+1))));
				//tmp2存放由link_id映射到的node_id序列
				tmp2.add(g1.link_pair_to_node.get(new link_pair(tmp.get(j), tmp.get(j+1))));
			}
			tmp2.add(car_to);//完整的node_id序列应包含起点与终点
			for(int j=0;j<tmp2.size()-1;++j) {
				node_pair tmp_node=new node_pair(tmp2.get(j), tmp2.get(j+1));
//				System.out.println(tmp2.get(j)+" "+tmp2.get(j+1));
				g1.node_pair_to_link.get(tmp_node).update_cost(cars.size()/0.3);
				++g1.node_pair_to_link.get(tmp_node).used_cnt;
			}
			
		}
		
		for(int i=preset_cars.size()-preset_limit+2;i<preset_cars.size();++i) {
			if(preset_cars.get(i).priority==1) {
				priority_cars.add(preset_cars.get(i));
			}else {
				normal_cars.add(preset_cars.get(i));
			}
		}
		
		//挑一些车子先跑
		Collections.sort(priority_cars);
		time_line=0;
		for(int i=0;i<(s_time+30)*16;++i) {
			time_line=r.nextInt(s_time+25);
			int cnt3=0;
			while(g1.preset_time_to_cnt.containsKey(time_line)&&g1.preset_time_to_cnt.get(time_line)>20) {
				time_line=r.nextInt(s_time+25);
				++cnt3;
				if(cnt3==5) break;
			}
			if(!g1.preset_time_to_cnt.containsKey(time_line))
				g1.preset_time_to_cnt.put(time_line, 1);
			else
				g1.preset_time_to_cnt.put(time_line, 1+g1.preset_time_to_cnt.get(time_line));
			if(time_line<priority_cars.get(i).start_time) time_line=priority_cars.get(i).start_time;
			pout.print("("+priority_cars.get(i).car_id+","+time_line+",");
			int s=priority_cars.get(i).from,t=priority_cars.get(i).to;
			g1.Yen_ksp(s, t, Constant.candidate_path);//计算若干条备选路
//			System.out.println(s+"->"+t);
			Path p=g1.ans_path.get(r.nextInt(g1.ans_path.size()));
			priority_cars.get(i).path=p;
			//对车子的路径进行相似度计算,两个序列的协方差???(序列长度未必一样)
			//统计每条边被用了多少次
			for(int j=0;j<p.path.size()-1;++j) {
				node_pair tmp_node=new node_pair(p.path.get(j),p.path.get(j+1));
				road_link tmp_link=g1.node_pair_to_link.get(tmp_node);
				++tmp_link.used_cnt;
				g1.ib_mp.put(tmp_link.road_id, true);
				tmp_link.used_list.add(1);
			}
//			for(road_link tmp:g1.node_pair_to_link.values()) {
//				if(!g1.ib_mp.containsKey(tmp.road_id)) tmp.used_list.add(0);
//			}
			for(node_pair tmp:g1.node_pair_to_link.keySet()) {
				if(!g1.ib_mp.containsKey(g1.node_pair_to_link.get(tmp).road_id))
					g1.node_pair_to_link.get(tmp).used_list.add(0);
			}
			//打印路径
			for(int j=0;j<p.path.size()-2;++j) {
				pout.print(g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).road_id+",");
			}
			pout.println(g1.node_pair_to_link.get(new node_pair(p.path.get(p.path.size()-2),p.path.get(p.path.size()-1))).road_id+")");

			//根据每条road_link的used_cnt进行反馈,调整链路的cost
			if(priority_cars.get(i).is_hard) {
				for(int j=0;j<p.path.size()-1;++j) {
					g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).update_cost(cars.size()/1.2);
				}
			}else {
				for(int j=0;j<p.path.size()-1;++j) {
					g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).update_cost(cars.size());
				}
			}
			g1.clear();
		}
		
		//先安排优先车辆（非预置的剩下的）
		for(int i=(s_time+30)*16;i<priority_cars.size();++i) {
			time_line=(r.nextInt(55)+(i-(s_time+30)*16)/4233*88+s_time+25);
			if(time_line<priority_cars.get(i).start_time) time_line=priority_cars.get(i).start_time;
			pout.print("("+priority_cars.get(i).car_id+","+time_line+",");
			int s=priority_cars.get(i).from,t=priority_cars.get(i).to;
			g1.Yen_ksp(s, t, Constant.candidate_path);//计算若干条备选路
//			System.out.println(s+"->"+t);
			Path p=g1.ans_path.get(r.nextInt(g1.ans_path.size()));
			normal_cars.get(i).path=p;
			//对车子的路径进行相似度计算,两个序列的协方差???(序列长度未必一样)
			//统计每条边被用了多少次
			for(int j=0;j<p.path.size()-1;++j) {
				node_pair tmp_node=new node_pair(p.path.get(j),p.path.get(j+1));
				road_link tmp_link=g1.node_pair_to_link.get(tmp_node);
				++tmp_link.used_cnt;
				g1.ib_mp.put(tmp_link.road_id, true);
				tmp_link.used_list.add(1);
			}
//			for(road_link tmp:g1.node_pair_to_link.values()) {
//				if(!g1.ib_mp.containsKey(tmp.road_id)) tmp.used_list.add(0);
//			}
			for(node_pair tmp:g1.node_pair_to_link.keySet()) {
				if(!g1.ib_mp.containsKey(g1.node_pair_to_link.get(tmp).road_id))
					g1.node_pair_to_link.get(tmp).used_list.add(0);
			}
			//打印路径
			for(int j=0;j<p.path.size()-2;++j) {
				pout.print(g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).road_id+",");
			}
			pout.println(g1.node_pair_to_link.get(new node_pair(p.path.get(p.path.size()-2),p.path.get(p.path.size()-1))).road_id+")");

			//根据每条road_link的used_cnt进行反馈,调整链路的cost
			if(priority_cars.get(i).is_hard) {
				for(int j=0;j<p.path.size()-1;++j) {
					g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).update_cost(cars.size()/1.2);
				}
			}else {
				for(int j=0;j<p.path.size()-1;++j) {
					g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).update_cost(cars.size());
				}
			}
			g1.clear();
		}
		//安排普通优先级的车辆（非预置）
		Collections.sort(normal_cars);
		for(int i=0;i<normal_cars.size();++i) {
			pout.print("("+normal_cars.get(i).car_id+","+(r.nextInt(55)+i/4233*88+time_line)+",");
			int s=normal_cars.get(i).from,t=normal_cars.get(i).to;
			g1.Yen_ksp(s, t, Constant.candidate_path);//计算若干条备选路
//			System.out.println(s+"->"+t);
			Path p=g1.ans_path.get(r.nextInt(g1.ans_path.size()));
			normal_cars.get(i).path=p;
			//对车子的路径进行相似度计算,两个序列的协方差???(序列长度未必一样)
			//统计每条边被用了多少次
			for(int j=0;j<p.path.size()-1;++j) {
				node_pair tmp_node=new node_pair(p.path.get(j),p.path.get(j+1));
				road_link tmp_link=g1.node_pair_to_link.get(tmp_node);
				++tmp_link.used_cnt;
				g1.ib_mp.put(tmp_link.road_id, true);
				tmp_link.used_list.add(1);
			}
//			for(road_link tmp:g1.node_pair_to_link.values()) {
//				if(!g1.ib_mp.containsKey(tmp.road_id)) tmp.used_list.add(0);
//			}
			for(node_pair tmp:g1.node_pair_to_link.keySet()) {
				if(!g1.ib_mp.containsKey(g1.node_pair_to_link.get(tmp).road_id))
					g1.node_pair_to_link.get(tmp).used_list.add(0);
			}
			
			for(int j=0;j<p.path.size()-2;++j) {
				pout.print(g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).road_id+",");
			}
			pout.println(g1.node_pair_to_link.get(new node_pair(p.path.get(p.path.size()-2),p.path.get(p.path.size()-1))).road_id+")");

			//根据每条road_link的used_cnt进行反馈调整链路的cost
			if(normal_cars.get(i).is_hard) {
				for(int j=0;j<p.path.size()-1;++j) {
					g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).update_cost(cars.size()/1.2);
				}
			}else {
				for(int j=0;j<p.path.size()-1;++j) {
					g1.node_pair_to_link.get(new node_pair(p.path.get(j),p.path.get(j+1))).update_cost(cars.size());
				}
			}
			g1.clear();
		}
		
		pout.close();
		
		//输出一下统计数据
		//起点与终点数据,看起来分布还是不太均匀的,不如就按照统计得到的顺序来好了
		for(node_pair tmp:g1.ni_mp.keySet()) {
			System.out.printf("(%d,%d) : %d\n",tmp.src_id,tmp.dst_id,g1.ni_mp.get(tmp));
		}
		System.out.println();
		//输出每条边被用了多少次,如果要基于此对链路的cost进行优化的话,应该在node_pair_to_link_tmp上操作
		System.out.printf("total link number: %d\n",g1.node_pair_to_link.size());
		for(node_pair tmp:g1.node_pair_to_link.keySet()) {
			System.out.printf("link_%d : %d, %.3f\n",g1.node_pair_to_link.get(tmp).road_id,
					g1.node_pair_to_link.get(tmp).used_cnt,g1.node_pair_to_link_tmp.get(tmp));
//			System.out.println(g1.node_pair_to_link.get(tmp).used_list.size());
//			if(g1.node_pair_to_link.get(tmp).road_id==5040) {
//				for(int i=0;i<g1.node_pair_to_link.get(tmp).used_list.size();++i) {
//					System.out.println(g1.node_pair_to_link.get(tmp).used_list.get(i));
//				}
//				System.out.println();
//			}
		}
		System.out.println(cnt);
	}
	
	public static void main(String[] args) {
//		System.out.println(System.getProperty("user.dir"));
		String carPath = "config_1/car.txt";
		String roadPath = "config_1/road.txt";
		String crossPath ="config_1/cross.txt";
		String preAnsPath="config_1/presetAnswer.txt";
		String answerPath = "config_1/answer.txt";
		calc4(carPath,roadPath,crossPath,preAnsPath,answerPath);
		
	}
}
