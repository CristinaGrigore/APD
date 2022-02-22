#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <pthread.h>
#include "genetic_algorithm.h"

#define MIN(a, b) (a <= b)?a:b;
int read_input(sack_object **objects, int *object_count, int *sack_capacity, int *generations_count, int *P,int argc, char *argv[])
{
	FILE *fp;

	if (argc < 4) {
		fprintf(stderr, "Usage:\n\t./tema1 in_file generations_count P\n");
		return 0;
	}

	fp = fopen(argv[1], "r");
	if (fp == NULL) {
		return 0;
	}

	if (fscanf(fp, "%d %d", object_count, sack_capacity) < 2) {
		fclose(fp);
		return 0;
	}

	if (*object_count % 10) {
		fclose(fp);
		return 0;
	}

	sack_object *tmp_objects = (sack_object *) calloc(*object_count, sizeof(sack_object));

	for (int i = 0; i < *object_count; ++i) {
		if (fscanf(fp, "%d %d", &tmp_objects[i].profit, &tmp_objects[i].weight) < 2) {
			free(objects);
			fclose(fp);
			return 0;
		}
	}

	fclose(fp);

	*generations_count = (int) strtol(argv[2], NULL, 10);
	*P = atoi(argv[3]);

	if (*generations_count == 0) {
		free(tmp_objects);

		return 0;
	}

	*objects = tmp_objects;

	return 1;
}
void print_objects(const sack_object *objects, int object_count)
{
	for (int i = 0; i < object_count; ++i) {
		printf("%d %d\n", objects[i].weight, objects[i].profit);
	}
}

void print_generation(const individual *generation, int limit)
{
	for (int i = 0; i < limit; ++i) {
		for (int j = 0; j < generation[i].chromosome_length; ++j) {
			printf("%d ", generation[i].chromosomes[j]);
		}

		printf("\n%d - %d\n", i, generation[i].fitness);
	}
}

void print_best_fitness(const individual *generation)
{
	printf("%d\n", generation[0].fitness);
}

void compute_fitness_function(const sack_object *objects, individual *generation, int start, int end, int sack_capacity)
{
	int weight;
	int profit;

	for (int i = start; i < end; ++i) {
		weight = 0;
		profit = 0;

		for (int j = 0; j < generation[i].chromosome_length; ++j) {
			if (generation[i].chromosomes[j]) {
				weight += objects[j].weight;
				profit += objects[j].profit;
			}
		}

		generation[i].fitness = (weight <= sack_capacity) ? profit : 0;
	}
}

int cmpfunc(const void *a, const void *b)
{
	int i;
	individual *first = (individual *) a;
	individual *second = (individual *) b;

	int res = second->fitness - first->fitness; // decreasing by fitness
	if (res == 0) {
		int first_count = 0, second_count = 0;

		for (i = 0; i < first->chromosome_length && i < second->chromosome_length; ++i) {
			first_count += first->chromosomes[i];
			second_count += second->chromosomes[i];
		}

		res = first_count - second_count; // increasing by number of objects in the sack
		if (res == 0) {
			return second->index - first->index;
		}
	}

	return res;
}

void mutate_bit_string_1(const individual *ind, int generation_index)
{
	//printf("a1\n");
	int i, mutation_size;
	int step = 1 + generation_index % (ind->chromosome_length - 2);
	//printf("a1\n");
	if (ind->index % 2 == 0) {
		// for even-indexed indivi
		//printf("a2\n");
		//duals, mutate the first 40% chromosomes by a given step
		mutation_size = ind->chromosome_length * 4 / 10;
		for (i = 0; i < mutation_size; i += step) {
			ind->chromosomes[i] = 1 - ind->chromosomes[i];
		}
		//printf("a3\n");
	} else {
		//printf("else\n");
		// for even-indexed individuals, mutate the last 80% chromosomes by a given step
		mutation_size = ind->chromosome_length * 8 / 10;
		for (i = ind->chromosome_length - mutation_size; i < ind->chromosome_length; i += step) {
			ind->chromosomes[i] = 1 - ind->chromosomes[i];
		}
		//printf("end else for\n");
	}
}

void mutate_bit_string_2(const individual *ind, int generation_index)
{
	int step = 1 + generation_index % (ind->chromosome_length - 2);

	// mutate all chromosomes by a given step
	for (int i = 0; i < ind->chromosome_length; i += step) {
		ind->chromosomes[i] = 1 - ind->chromosomes[i];
	}
}

void crossover(individual *parent1, individual *child1, int generation_index)
{
	individual *parent2 = parent1 + 1;
	individual *child2 = child1 + 1;
	
	int count = 1 + generation_index % parent1->chromosome_length;

	memcpy(child1->chromosomes, parent1->chromosomes, count * sizeof(int));
	memcpy(child1->chromosomes + count, parent2->chromosomes + count, (parent1->chromosome_length - count) * sizeof(int));

	memcpy(child2->chromosomes, parent2->chromosomes, count * sizeof(int));
	memcpy(child2->chromosomes + count, parent1->chromosomes + count, (parent1->chromosome_length - count) * sizeof(int));
}

void copy_individual(const individual *from, const individual *to)
{
	if(to == NULL)
		printf("to null\n");
	else if(from == NULL)
		printf("from null\n");
	memcpy(to->chromosomes, from->chromosomes, from->chromosome_length * sizeof(int));
}

void free_generation(individual *generation)
{
	int i;

	for (i = 0; i < generation->chromosome_length; ++i) {
		free(generation[i].chromosomes);
		generation[i].chromosomes = NULL;
		generation[i].fitness = 0;
	}
}
void oets_func(individual *ind, int thread_id, int start, int end, pthread_barrier_t *barrier, int N)
{
	individual aux;
	if(barrier ==NULL)
		printf("NULL\n");
	int start_even, start_odd;
	// int end_even, end_odd;
	if (start % 2 == 0) {
		start_even = start;
		start_odd = start + 1;
	} else {
		start_even = start + 1;
		start_odd = start;
	}
	int i, j;
	for(i = 0; i < N; i++) {
		for (j = start_even; j < end && j < N - 1; j += 2) {
			if(cmpfunc((void *)(ind + j), (void *)(ind + j + 1)) > 0) {
			//if (v[j] > v[j + 1]) {
				aux = ind[j];
				ind[j] = ind[j + 1];
				ind[j + 1] = aux;
			}
		}
		int r = pthread_barrier_wait(barrier);
		// if(r)
		// 	printf("ERRRORR %d\n", r);

		// printf("%d) done first barrier\n", thread_id);
		for (j = start_odd; j < end && j < N - 1; j += 2) {
			if (cmpfunc((void *)(ind + j), (void *)(ind + j + 1)) > 0) {
				aux = ind[j];
				ind[j] = ind[j + 1];
				ind[j + 1] = aux;
			}
		}
		r = pthread_barrier_wait(barrier);
		
	}

}
void merge(int *source, int start, int mid, int end, int *destination) {
	int iA = start;
	int iB = mid;
	int i;

	for (i = start; i < end; i++) {
		if (end == iB || (iA < mid && source[iA] <= source[iB])) {
			destination[i] = source[iA];
			iA++;
		} else {
			destination[i] = source[iB];
			iB++;
		}
	}
}
void *run_genetic_algorithm(void *arguments)
{
	int count, cursor;
	
	struct_arguments *args = (struct_arguments *)arguments;

	individual *tmp = NULL;
	//getting arguments
	sack_object *objects = args->arg1;
	// number of objects
	int object_count = args->arg2;
	// maximum weight that can be carried in the sack
	int generations_count = args->arg3;
	// number of generations
	int P = args->arg4;
	//number of threads
	int sack_capacity = args->arg5;

	int thread_id = (args->arg6);

	pthread_barrier_t *barrier = args->barrier;
	
	pthread_mutex_t *mutex = args->mutex;

	individual *current_generation = args->current;
	individual *next_generation = args->next;
	
	int start = thread_id * (double)object_count / P;
	int end = MIN((thread_id + 1) * (double)object_count / P, object_count);

	//printf("%d) obj count %d, start %d, end %d\n", thread_id, object_count, start, end);
	// set initial generation (composed of object_count individuals with a single item in the sack)
	for (int i = start; i < end; ++i) {
		current_generation[i].fitness = 0;
		current_generation[i].chromosomes = (int*) calloc(object_count, sizeof(int));
		current_generation[i].chromosomes[i] = 1;
		current_generation[i].index = i;
		current_generation[i].chromosome_length = object_count;

		next_generation[i].fitness = 0;
		next_generation[i].chromosomes = (int*) calloc(object_count, sizeof(int));
		next_generation[i].index = i;
		next_generation[i].chromosome_length = object_count;
	}
	//printf("bar 1\n");
	pthread_barrier_wait(barrier);
	//printf("bar 1\n");
	// iterate for each generation
	for (int k = 0; k < generations_count; ++k) {
		cursor = 0;

		// compute fitness and sort by it
		compute_fitness_function(objects, current_generation, start, end, sack_capacity);
		//qsort(current_generation + start, (end -  start), sizeof(individual), cmpfunc);
		pthread_barrier_wait(barrier);
		printf("for gen %d\n", k);
		if(thread_id == 0)
			print_generation(current_generation + start, end - start);
		oets_func(current_generation + start, thread_id, start, end, barrier, end - start);
		printf("\n");
		//pthread_barrier_wait(barrier);
		print_generation(current_generation + start, end - start);
		//count = object_count * 3 / 10;
		count = start + (end - start) * 3 / 10;
		///printf("%d) copy from %d to %d\n",thread_id,start,  count );
		for (int i = start; i < count; ++i) {
			copy_individual(current_generation + i, next_generation + i);
		}
		
		//printf("bar 3\n");
		cursor = (end - start) * 3 / 10;;
		pthread_barrier_wait(barrier);
		//mutate first 20% children with the first version of bit string mutation
		count = start + (end - start) * 2 / 10;
		//printf("%d) mutate1 from %d to %d\n",thread_id,start,  count );
		//count = object_count * 2 / 10;
		for (int i = start; i < count; ++i) {
			//printf("try copy %d to %d\n", i, cursor + i);
			copy_individual(current_generation + i, next_generation + cursor + i);
			
			mutate_bit_string_1(next_generation + cursor + i, k);
		}
		
		cursor += (end - start) * 2 / 10;
		pthread_barrier_wait(barrier);
		// mutate next 20% children with the second version of bit string mutation
		count = start + (end - start) * 2 / 10;
		//printf("%d) mutate2 from %d to %d\n",thread_id,start,  count );
		//count = object_count * 2 / 10;

		for (int i = start; i < count; ++i) {
			//printf("%d) copy from %d to %d\n",thread_id,i + (end - start) * 2 / 10, cursor + i );
			copy_individual(current_generation + i + (end - start) * 2 / 10, next_generation + cursor + i);

			mutate_bit_string_2(next_generation + cursor + i, k);
		}
		
		cursor += (end - start) * 2 / 10;;
		pthread_barrier_wait(barrier);
		// crossover first 30% parents with one-point crossover
		// (if there is an odd number of parents, the last one is kept as such)
		count = start + (end - start) * 3 / 10;
		//printf("%d) crossover from %d to %d\n",thread_id,start,  count );
		//count = object_count * 3 / 10;
		if (count % 2 == 1) {
			copy_individual(current_generation + object_count - 1, next_generation + cursor + count - 1);
			count--;
		}
		pthread_barrier_wait(barrier);

		for (int i = start; i < count; i += 2) {
			crossover(current_generation + i, next_generation + cursor + i, k);
		}
		pthread_barrier_wait(barrier);
		// switch to new generation
		tmp = current_generation;
		current_generation = next_generation;
		next_generation = tmp;

		pthread_barrier_wait(barrier);

		for (int i = start; i < end; ++i) {
			current_generation[i].index = i;
		}

		if (k % 5 == 0 && thread_id == 0) {
			print_best_fitness(current_generation);
		}
	}
	if(thread_id == 0)
		print_best_fitness(current_generation);
	//print_generation(current_generation, object_count);
	
}