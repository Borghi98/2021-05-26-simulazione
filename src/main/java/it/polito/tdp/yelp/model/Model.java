package it.polito.tdp.yelp.model;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private Graph<Business, DefaultWeightedEdge> grafo; 
	private List<Business> vertici;
	private Map<String,Business> verticiIdMap;
	
	//variabili ricorsione
	private List<Business> percorsoBest ;

	
	public List<String> getAllCities(){
		YelpDao dao = new YelpDao();
		return dao.getAllCities();
	}
	
	public String creaGrafo(String city, Year anno) {
		this.grafo = new SimpleDirectedWeightedGraph<Business,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		// creo vertici 
		YelpDao dao = new YelpDao();
		this.vertici = dao.getAllBusinessByCityAndYear(city, anno);
		
		this.verticiIdMap = new HashMap<>();
		for(Business b : this.vertici)
			this.verticiIdMap.put(b.getBusinessId(), b) ;
		
		Graphs.addAllVertices(this.grafo, this.vertici);
		
		//creo i vertici
		List<ArcoGrafo> archi = dao.calcolaArchi(city, anno) ;
		for(ArcoGrafo arco : archi) {
			Graphs.addEdge(this.grafo,
					this.verticiIdMap.get(arco.getBusinessId1()),
					this.verticiIdMap.get(arco.getBusinessId2()), 
					arco.getPeso()) ;
		}
		
		return String.format("Grafo creato con %d vertici e %d archi\n",
				this.grafo.vertexSet().size(),
				this.grafo.edgeSet().size()) ;
	}
	
	public Business getLocaleMigliore() {
		double max = 0.0 ;
		Business result = null; 
		for(Business b:this.grafo.vertexSet()) {
			double val = 0.0;
			for(DefaultWeightedEdge e : this.grafo.incomingEdgesOf(b)) {
				val += this.grafo.getEdgeWeight(e);
			}
			for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(b)) {
				val -= this.grafo.getEdgeWeight(e);
			}
			if(val>max) {
				max = val ;
				result = b ;
			}
		}
		return result; 
	}
	
	public List<Business> percorsoMigliore(Business partenza, Business arrivo, double soglia){
		this.percorsoBest = null; 
		List<Business> parziale = new ArrayList<Business>();
		parziale.add(partenza);
		
		cerca(parziale, 1, arrivo, soglia);
		
		return this.percorsoBest;
	}
	
	private void cerca(List<Business>parziale, int livello, Business arrivo, double soglia) {
		
		Business ultimo = parziale.get(parziale.size()-1);
		// caso terminale, trovato arrivo
		if(ultimo.equals(arrivo)) {
			if(this.percorsoBest==null) {
				this.percorsoBest = new ArrayList<>(parziale);
				return ;
			} else if( parziale.size() < this.percorsoBest.size()) {
				this.percorsoBest = new ArrayList<>(parziale);
				return;
			} else {
				return;
			}
		}
		// genero percorso
		// cerco successori di ultimo 
		for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(ultimo)) {
			if(this.grafo.getEdgeWeight(e)>soglia) {
				
				Business prossimo = Graphs.getOppositeVertex(this.grafo, e, ultimo);
				 
				
				if(!parziale.contains(prossimo)) { // evita i cicli
					parziale.add(prossimo);
					cerca(parziale, livello + 1, arrivo, soglia);
					parziale.remove(parziale.size()-1) ;
				}
			}
		}
		
	}
	
	public List<Business> getVertici() {
		return this.vertici ;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
