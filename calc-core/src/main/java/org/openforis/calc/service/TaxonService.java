/**
 * 
 */
package org.openforis.calc.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.openforis.calc.model.Taxon;
import org.openforis.calc.persistence.TaxonDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Mino Togna
 * 
 */
@Service
@Lazy
public class TaxonService extends CalcService {

	@Autowired
	private TaxonDao taxonDao;

	private List<Taxon> taxa;

	public TaxonService() {
	}

	public Taxon findByTaxonCode(String taxonCode) {
		Taxon taxon = null;

		for ( Taxon t : taxa ) {
			if ( t.getTaxonCode().equals(taxonCode) ) {
				taxon = t;
				break;
			}
		}

		return taxon;
	}

	@PostConstruct
	protected void init() {
		taxa = taxonDao.findAll();
	}

}
