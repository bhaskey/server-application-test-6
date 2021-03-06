package com.mytaxi.service.car;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mytaxi.dataaccessobject.CarRepository;
import com.mytaxi.domainobject.CarDO;
import com.mytaxi.exception.CarAlreadyInUseException;
import com.mytaxi.exception.ConstraintsViolationException;
import com.mytaxi.exception.EntityNotFoundException;

/**
 * Service to encapsulate the link between DAO and controller and to have business logic for some car specific things.
 * <p/>
 */
@Service
public class DefaultCarService implements CarService{
	
	private static org.slf4j.Logger LOG = LoggerFactory.getLogger(DefaultCarService.class);
	
	private final CarRepository carRepository;
	
	public DefaultCarService(final CarRepository carRepository){
		this.carRepository = carRepository;
	}

    /**
     * @param carId
     * @return CarDO
     * @throws EntityNotFoundException
     */
	@Override
	public CarDO find(Long carId) throws EntityNotFoundException {
    	return findCarChecked(carId);
	}

	/**
	 * @param CarDO
     * @return CarDO
     * @throws ConstraintsViolationException
	 */
	@Override
	public CarDO create(CarDO carDO) throws ConstraintsViolationException {
		CarDO car;
		try{
			car = carRepository.save(carDO);
		}
		catch (DataIntegrityViolationException e)
        {
            LOG.warn("Some constraints are thrown due to car creation", e);
            throw new ConstraintsViolationException(e.getMessage());
        }		
		return car;
	}

	/**
	 * @param CarDO
     * @throws EntityNotFoundException
	 */
	@Override
	@Transactional
	public void delete(Long carId) throws EntityNotFoundException {
		CarDO car = findCarChecked(carId);
		car.setDeleted(true);
	}
	
	/**
	 * @param carId
     * @throws CarAlreadyInUseException, EntityNotFoundException
	 */
	@Override
	@Transactional
	public void select(Long carId) throws CarAlreadyInUseException, EntityNotFoundException
	{
		CarDO carDO = findCarChecked(carId);
		if(!carDO.getIsAvailable()){
			throw new CarAlreadyInUseException("Car with id: " + carId + "is already mapped to another driver");
		}
		carDO.setIsAvailable(false);
	}
	
	/**
	 * @param carId
     * @throws EntityNotFoundException
	 */
	@Override
	@Transactional
	public void deSelect(Long carId) throws EntityNotFoundException
	{
		CarDO carDO = findCarChecked(carId);
		carDO.setIsAvailable(true);
	}

	/**
	 * @param Boolean
	 * @return List<CarDO>
	 */
	@Override
	public List<CarDO> find(Boolean isAvailable) {
		return carRepository.findByIsAvailable(isAvailable);
	}
	
	private CarDO findCarChecked(Long carId) throws EntityNotFoundException
    {
        CarDO carDO = carRepository.findOne(carId);
        if (carDO == null)
        {
            throw new EntityNotFoundException("Could not find entity with id: " + carId);
        }
        return carDO;
    }

}
