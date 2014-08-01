#include <cuda.h>
#include <cuda_runtime.h>
#include "matrix_mul.h"
#include <math.h>
#include <stdio.h>
#define TILE_WIDTH 16
    namespace cuda
    {
/* update 16*1 result vector at the same time*/
      __device__ void update(float *a, float b, float *c)
      {
        for (int i = 0; i < 16; i++)
          c[i] += a[i * 4] * b;
      }
/*
 * kernel function
 * 16*8 block to calculate block of 16 * 128
 * a boundary issue still exist while using float4 data type
 */    
 __global__ void matrix_mul_kernel (float *a, float *b, float *c, int n)
 {
 /* use shared memory to hold the transpose of a
  * 16 x 16 sub-matrix of 1 x 4 sub-vectors of a
  */
  __shared__ float as[16][65];
 /*  registers for 16 * 1 of c sub-matrix */
  float cr[16] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
  float4 zero = make_float4(0.0,0.0,0.0,0.0);
/*
 * local variables for each thread to determine which data to fetch and which block to calculate
 */
 int nDiv64 = (n-1)/64+1;
 int sRow = threadIdx.y;
 int sRow4 = sRow*4;
 int sCol = threadIdx.x;
 int tid = sRow*16+sCol;
 int aNext = (16*blockIdx.y+sRow)*n+sCol*4;
 int bNext = 128*blockIdx.x + tid;
 int cNext = 16*blockIdx.y*n + 128*blockIdx.x + tid;
 int nTimes2 = 2*n;
 int nTimes3 = 3*n;
 int nTimes4 = 4*n;
 a += aNext;
 b += bNext;
 c += cNext;
 float4 *a4 = (float4 *)a;
  /*
   * Multiple 16*64 As and 64*16 Bs and then update a 16*1 C 
   */
   for (int i = 0; i < nDiv64; i++)
   {
   //if the next 4 number is not null
    if ( i*64 + sCol *4 < n && blockIdx.y * 16 + sRow < n)
    {
     // if next 4 number is full
      if( i*64 + sCol *4 + 3 < n ){
        *( (float4 *)(&as[sCol][sRow4]) ) = a4[0];
      }
      else
      { 
        // if only 3 numbers 
        if( i*64 + sCol *4 + 2 < n ){
          *( (float4 *)(&as[sCol][sRow4]) ) = make_float4(a4[0].x,a4[0].y,a4[0].z,0.0);
        }
          //if only 2 numbers
        else{ 
          if( i*64 + sCol *4 + 1 < n ){
            *( (float4 *)(&as[sCol][sRow4]) ) = make_float4(a4[0].x,a4[0].y,0.0,0.0);
          }
          else{
            *( (float4 *)(&as[sCol][sRow4]) ) = make_float4(a4[0].x,0.0,0.0,0.0);
          }
        }
      }

    }
    /* if the next 4 numbers are all out of boundary */
    else{
      *( (float4 *)(&as[sCol][sRow4]) ) = zero;
    }
    /* The next 8 rows , same operation as the previous block */
    if ( i*64 + sCol *4 < n && blockIdx.y * 16 + 8 + sRow < n)
    {
      // if next 4 number is not null
      if( i*64 + sCol *4 + 3 < n)
        *( (float4 *)(&as[sCol][sRow4 + 32]) ) = a4[nTimes2];
      else
      {
        // if only 3 numbers
        if( i*64 + sCol *4 + 2 < n ){
          *( (float4 *)(&as[sCol][sRow4 + 32]) ) = make_float4(a4[nTimes2].x,a4[nTimes2].y,a4[nTimes2].z,0.0);
        }
        //if only 2 numbers
        else{
          if( i*64 + sCol *4 + 1 < n ){
            *( (float4 *)(&as[sCol][sRow4 + 32]) ) = make_float4(a4[nTimes2].x,a4[nTimes2].y,0.0,0.0);
          }
          else{
            *( (float4 *)(&as[sCol][sRow4 + 32]) ) = make_float4(a4[nTimes2].x,0.0,0.0,0.0);
          }
        }
      }

    }
    else{
      *( (float4 *)(&as[sCol][sRow4+32]) ) = zero;
    }
    /* wait for read to complete fetching subA */
    __syncthreads(); // wait for read to complete

    /* Begin fetching subB*/
    /* each thread fetch 4*1 at one time and do 16 iteration */
    float br0;
    float br1;
    float br2;
    float br3;
    int boundary_row = blockIdx.x * 128 + threadIdx.y * 16 + threadIdx.x;
    if( boundary_row < n && 64 * i <n)
      br0 = b[0];
    else
      br0 = 0.0;
    if ( boundary_row < n && 64 * i + 1  < n)
      br1 = b[n];
    else
      br1 = 0.0;
    if (boundary_row < n && 64 * i + 2  < n)
      br2 = b[nTimes2];
    else
      br2 = 0.0;
    if (boundary_row < n && 64 * i + 3  < n)
      br3 = b[nTimes3];
    else 
      br3 = 0.0;

    b += nTimes4;

      #pragma unroll
    for (int k = 0; k < 15; k++)
    {
      update (&as[k][0], br0, cr); 
      if( boundary_row < n && 64*i + (k+1)*4   < n )
        br0 = b[0];
      else
        br0 = 0.0;
      update (&as[k][1], br1, cr); 
      if( boundary_row < n && 64*i + (k+1)*4 + 1  < n )
        br1 = b[n];
      else
        br1 = 0.0;
      update (&as[k][2], br2, cr); br2 = b[nTimes2];
      if( boundary_row < n && 64*i + (k+1)*4 + 2 < n )
        br2 = b[nTimes2];
      else
        br2 = 0.0;
      update (&as[k][3], br3, cr); br3 = b[nTimes3];
      if( boundary_row < n && 64*i + (k+1)*4 +  3 < n)
        br3 = b[nTimes3];
      else
        br3 = 0.0;

      b+= nTimes4;
    }
          /* update result value */
    update (&as[15][0], br0, cr);
    update (&as[15][1], br1, cr);
    update (&as[15][2], br2, cr);
    update (&as[15][3], br3, cr);
    a4 += 16;
    __syncthreads(); // wait for computation to complete
  }
  /* return the result value
   * Do not update if out of boundary 
   */
  if(blockIdx.x * 128 + threadIdx.y * 16 + threadIdx.x < n){
    for (int j = 0; j < 16; j++)
    {
      if(blockIdx.y*16 + j < n){
        c[0] = cr[j];
        c += n; 
      }
    }
  }

}
/* Kernel for small matrix*/
__global__ void matrix_mul_kernel_small_matrix(float* A, float* B, float* C, int sq_dimension){

  float CValue = 0;

  int Row = blockIdx.y*TILE_WIDTH + threadIdx.y;
  int Col = blockIdx.x*TILE_WIDTH + threadIdx.x;
  int Dim = TILE_WIDTH;
  
    __shared__ float As[TILE_WIDTH][TILE_WIDTH + 1]; // avoid bank conflict
    __shared__ float Bs[TILE_WIDTH][TILE_WIDTH];

    for (int k = 0; k < (TILE_WIDTH + sq_dimension - 1)/TILE_WIDTH; k++) {

     if (k*TILE_WIDTH + threadIdx.x < sq_dimension && Row < sq_dimension)   
      As[threadIdx.y][threadIdx.x] = A[Row*sq_dimension + k*TILE_WIDTH + threadIdx.x];
    else                                                   
      As[threadIdx.y][threadIdx.x] = 0.0;

    if (k*TILE_WIDTH + threadIdx.y < sq_dimension && Col < sq_dimension)   
      Bs[threadIdx.y][threadIdx.x] = B[(k*TILE_WIDTH + threadIdx.y)*sq_dimension + Col];
    else                                                   
      Bs[threadIdx.y][threadIdx.x] = 0.0;

    __syncthreads();
    for (int n = 0; n < Dim; ++n) 
      CValue += As[threadIdx.y][n] * Bs[n][threadIdx.x];


    __syncthreads();
  }

  if (Row < sq_dimension && Col < sq_dimension) 
    C[((blockIdx.y * blockDim.y + threadIdx.y)*sq_dimension)+(blockIdx.x*blockDim.x)+threadIdx.x]=CValue;
}
void 
matrix_multiplication(float *sq_matrix_1, float *sq_matrix_2, float *sq_matrix_result, unsigned int sq_dimension)
{
  int size = sq_dimension * sq_dimension * sizeof(float);
  float *sq_matrix_1_d, *sq_matrix_2_d, *sq_matrix_result_d;

    /***************************************************
  1st Part: Allocation of memory on device memory  
    ****************************************************/

    /* copy sq_matrix_1 and sq_matrix_2 to device memory */
  cudaMalloc((void**) &sq_matrix_1_d, size);
  cudaMemcpy(sq_matrix_1_d, sq_matrix_1, size, cudaMemcpyHostToDevice);
  cudaMalloc((void**) &sq_matrix_2_d, size);
  cudaMemcpy(sq_matrix_2_d, sq_matrix_2, size, cudaMemcpyHostToDevice);

    /*allocate sq_matrix_result on host */
  cudaMalloc((void**) &sq_matrix_result_d, size);

    /***************************************************
   2nd Part: Inovke kernel 
    ****************************************************/
   if(sq_dimension % 4 !=0){
    /* All matrix valid*/
    dim3 dimBlock(16, 16);
    dim3 dimGrid((sq_dimension-1)/16 + 1,(sq_dimension -1)/16+1);
    matrix_mul_kernel_small_matrix<<<dimGrid, dimBlock,dimBlock.x * dimBlock.y*sizeof(float)>>>(sq_matrix_1_d, sq_matrix_2_d, sq_matrix_result_d, sq_dimension);

  }
  else{
    /* Should be able to work with any input dimension, but does not work with odd numbers, still debugging */
   dim3 dimBlock(16, 8);
   dim3 dimGrid((sq_dimension-1)/128 + 1,(sq_dimension -1)/16+1);
   matrix_mul_kernel<<<dimGrid, dimBlock,dimBlock.x * dimBlock.y*sizeof(float)>>>(sq_matrix_1_d, sq_matrix_2_d, sq_matrix_result_d, sq_dimension);
 }


    /***************************************************
   3rd Part: Transfer result from device to host 
    ****************************************************/
   cudaMemcpy(sq_matrix_result, sq_matrix_result_d, size, cudaMemcpyDeviceToHost);
   cudaFree(sq_matrix_1_d);
   cudaFree(sq_matrix_2_d);
   cudaFree(sq_matrix_result_d);
 }  
} // namespace cuda
